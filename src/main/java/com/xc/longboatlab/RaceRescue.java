package com.xc.longboatlab;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

/** Load the destination over server ticks before executing the map's checkpoint return. */
public final class RaceRescue {
    private static final String PENDING = "lr_rescue_pending";
    private static final ChunkTicketType<UUID> TICKET = ChunkTicketType.create(
            "longboat_race_rescue", Comparator.<UUID>naturalOrder(), 100);
    private record Request(ServerWorld world, ChunkPos center, int ring, int checkpoint, long started) {}
    private static final Map<UUID, Request> REQUESTS = new HashMap<>();
    private RaceRescue() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> REQUESTS.clear());
    }

    public static LiteralArgumentBuilder<ServerCommandSource> prepareCommand() {
        return CommandManager.literal("race_prepare")
                .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                .then(CommandManager.argument("checkpoint", IntegerArgumentType.integer(0, 4096))
                .executes(c -> prepare(c.getSource().getPlayerOrThrow(),
                        Vec3ArgumentType.getVec3(c, "position"), IntegerArgumentType.getInteger(c, "checkpoint")))));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> moveCommand() {
        return CommandManager.literal("race_move")
                .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                .then(CommandManager.argument("yaw", FloatArgumentType.floatArg(-360, 360))
                .executes(c -> move(c.getSource().getEntityOrThrow(),
                        Vec3ArgumentType.getVec3(c, "position"), FloatArgumentType.getFloat(c, "yaw")))));
    }

    private static int prepare(ServerPlayerEntity player, Vec3d target, int checkpoint) {
        if (!player.getCommandTags().contains("lr_racer") || REQUESTS.containsKey(player.getUuid())) return 0;
        double radius = 2;
        if (player.getVehicle() instanceof BoatEntity boat) {
            radius = Math.hypot(BoatGeometry.halfLength(boat), BoatGeometry.halfWidth(boat)) + 2;
            for (var oar : GiantOars.mounted(boat)) radius = Math.max(radius, oar.scale() * 2 + BoatGeometry.halfWidth(boat) + 2);
        }
        // Bound chunk work even for command-created boats with extreme dimensions.
        if (player.getCommandTags().contains("lr_replace_pending")) radius = 2;
        if (!Double.isFinite(radius) || radius > 96) {
            player.sendMessage(Text.literal("船体过大，救援范围不足；可用换船道具返回。"), false);
            return 0;
        }
        int ring = Math.max(1, (int) Math.ceil(radius / 16));
        Request request = new Request(player.getServerWorld(), new ChunkPos(BlockPos.ofFloored(target)),
                ring, checkpoint, player.getServerWorld().getTime());
        REQUESTS.put(player.getUuid(), request);
        player.addCommandTag(PENDING);
        ticket(request, player.getUuid());
        player.sendMessage(Text.literal("正在准备检查点，请稍候……"), true);
        return 1;
    }

    private static void ticket(Request request, UUID id) {
        // addTicket only requests work. getChunk/getChunkFutureSyncOnMainThread would block this tick.
        request.world.getChunkManager().addTicket(TICKET, request.center, request.ring + 2, id);
    }

    public static void tick(MinecraftServer server) {
        for (var iterator = REQUESTS.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next(); UUID id = entry.getKey(); Request r = entry.getValue();
            var player = server.getPlayerManager().getPlayer(id);
            boolean valid = player != null && player.isAlive() && player.getServerWorld() == r.world
                    && player.getCommandTags().contains(PENDING) && player.getCommandTags().contains("lr_racer");
            long age = r.world.getTime() - r.started;
            if (!valid || age > 600) {
                r.world.getChunkManager().removeTicket(TICKET, r.center, r.ring + 2, id);
                iterator.remove();
                if (player != null) {
                    player.removeCommandTag(PENDING); player.removeCommandTag("lr_replace_pending");
                    if (valid) player.sendMessage(Text.literal("检查点加载超时，本次未扣罚时；请稍后重试。"), false);
                }
                continue;
            }
            if (age % 20 == 0) ticket(r, id);
            boolean ready = true;
            for (int x = -r.ring; ready && x <= r.ring; x++) for (int z = -r.ring; z <= r.ring; z++) {
                if (!r.world.getChunkManager().isTickingFutureReady(ChunkPos.toLong(r.center.x + x, r.center.z + z))) {
                    ready = false; break;
                }
            }
            if (!ready) continue;
            iterator.remove();
            // The ticket expires after the client's new chunk subscription has had time to catch up.
            ticket(r, id);
            server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(2),
                    "function puffer_rally:return_ready_" + r.checkpoint);
            player.removeCommandTag(PENDING); player.removeCommandTag("lr_replace_pending");
        }
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (!REQUESTS.containsKey(player.getUuid())) {
                player.removeCommandTag(PENDING); player.removeCommandTag("lr_replace_pending");
            }
        }
    }

    private static int move(Entity entity, Vec3d target, float yaw) {
        if (!(entity instanceof BoatEntity boat) || !(boat.getWorld() instanceof ServerWorld world)) return 0;
        BoatAccess access = (BoatAccess) boat;
        var seats = access.longboat$seatData().copy();
        List<Entity> passengers = new ArrayList<>(boat.getPassengerList());
        for (Entity passenger : passengers) passenger.stopRiding();
        access.longboat$abilities().resetForRescue(boat);
        boat.refreshPositionAndAngles(target.x, target.y, target.z, yaw, 0);
        boat.setBoundingBox(BoatGeometry.bounds(boat));
        boat.prevX = boat.lastRenderX = target.x;
        boat.prevY = boat.lastRenderY = target.y;
        boat.prevZ = boat.lastRenderZ = target.z;
        boat.prevYaw = yaw;
        LoadedBoats.update(boat);
        for (Entity passenger : passengers) {
            if (passenger instanceof ServerPlayerEntity player) player.teleport(world, target.x, target.y, target.z, yaw, 0);
            else passenger.refreshPositionAndAngles(target.x, target.y, target.z, yaw, 0);
            passenger.setVelocity(Vec3d.ZERO); passenger.fallDistance = 0;
            passenger.startRiding(boat, true);
        }
        access.longboat$setSeatData(seats);
        BoatCollisionScene.invalidate(boat);
        return 1;
    }
}
