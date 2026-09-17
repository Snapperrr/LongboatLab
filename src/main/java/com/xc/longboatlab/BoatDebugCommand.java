package com.xc.longboatlab;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class BoatDebugCommand {
    private BoatDebugCommand() {}
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> dispatcher.register(
                CommandManager.literal("longboatlab").requires(source -> source.hasPermissionLevel(2))
                        .then(branch("spawn")).then(branch("give")).then(oarBranch()).then(RigBoatCommand.branch())
                        .then(RaceRescue.prepareCommand()).then(RaceRescue.moveCommand())
                        .then(CommandManager.literal("physics").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            if (!(player.getVehicle() instanceof BoatEntity boat)) {
                                throw new SimpleCommandExceptionType(Text.translatable("command.longboatlab.ride_first")).create();
                            }
                            context.getSource().sendFeedback(() -> Text.literal(BoatCollisionScene.diagnostics(boat)), false);
                            return 1;
                        }))));
    }
    private static LiteralArgumentBuilder<ServerCommandSource> oarBranch() {
        return CommandManager.literal("oar").then(CommandManager.argument("size",
                com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(1.001, Math.cbrt(Integer.MAX_VALUE)))
                .executes(context -> giveOar(context, 1))
                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64))
                        .executes(context -> giveOar(context, IntegerArgumentType.getInteger(context, "count")))));
    }
    private static int giveOar(CommandContext<ServerCommandSource> context, int count) throws CommandSyntaxException {
        double size = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(context, "size");
        if (!Double.isFinite(size)) return 0;
        int units = (int) Math.min(Integer.MAX_VALUE, Math.max(2, Math.round(size * size * size)));
        var player = context.getSource().getPlayerOrThrow();
        for (int i = 0; i < count; i++) {
            ItemStack stack = new ItemStack(net.minecraft.item.Items.WOODEN_SHOVEL);
            GiantOars.setData(stack, units, -1);
            GiantOars.setScale(stack, size);
            stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME, Text.translatable("item.longboatlab.giant_oar"));
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
        }
        context.getSource().sendFeedback(() -> Text.translatable("command.longboatlab.oar", size, count), false);
        return count;
    }
    private static LiteralArgumentBuilder<ServerCommandSource> branch(String mode) {
        var wood = CommandManager.argument("wood", StringArgumentType.word())
                .suggests((context, builder) -> {
                    for (BoatEntity.Type type : BoatEntity.Type.values()) builder.suggest(type.asString());
                    return builder.buildFuture();
                }).executes(context -> create(context, mode, StringArgumentType.getString(context, "wood")));
        var puffers = CommandManager.argument("puffers", IntegerArgumentType.integer(0))
                .executes(context -> create(context, mode, "oak")).then(wood);
        var right = CommandManager.argument("right", IntegerArgumentType.integer(0)).then(puffers);
        var left = CommandManager.argument("left", IntegerArgumentType.integer(0)).then(right);
        return CommandManager.literal(mode).then(CommandManager.argument("length", IntegerArgumentType.integer(1)).then(left));
    }
    private static int create(CommandContext<ServerCommandSource> context, String mode, String wood) throws CommandSyntaxException {
        BoatEntity.Type variant = null;
        for (BoatEntity.Type type : BoatEntity.Type.values()) if (type.asString().equals(wood)) variant = type;
        if (variant == null) throw new SimpleCommandExceptionType(Text.translatable("command.longboatlab.wood", wood)).create();
        var source = context.getSource();
        int length = IntegerArgumentType.getInteger(context, "length");
        int left = IntegerArgumentType.getInteger(context, "left");
        int right = IntegerArgumentType.getInteger(context, "right");
        int puffers = IntegerArgumentType.getInteger(context, "puffers");
        BoatRig rig = new BoatRig(length, OarRack.plain(left), OarRack.plain(right), true, puffers);
        var pos = source.getPosition().add(BoatGeometry.rotateY(new Vec3d(0, 0, 2), Math.toRadians(source.getRotation().y)));
        BoatEntity boat = new BoatEntity(source.getWorld(), pos.x, pos.y, pos.z);
        boat.setVariant(variant);
        boat.setYaw(source.getRotation().y);
        ((BoatAccess) boat).longboat$setRig(rig);
        if (mode.equals("give")) {
            ItemStack stack = new ItemStack(boat.asItem());
            rig.applyToStack(stack, source.getRegistryManager());
            var player = source.getPlayerOrThrow();
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
        } else {
            if (!BoatGeometry.spaceEmpty(boat, boat.getYaw())) {
                throw new SimpleCommandExceptionType(Text.translatable("message.longboatlab.no_space")).create();
            }
            if (!source.getWorld().spawnEntity(boat)) return 0;
        }
        source.sendFeedback(() -> Text.translatable("command.longboatlab.created", mode, length, left, right, puffers), false);
        return 1;
    }
}
