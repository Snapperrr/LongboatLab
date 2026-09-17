package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatBody;
import com.xc.longboatlab.PufferAttachments;
import com.xc.longboatlab.PufferGrid;
import com.xc.longboatlab.LoadedBoats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;

/** Frame-aligned exhaust billboards avoid the particle queue's extra tick of emitter latency. */
public final class BoatExhaust {
    private static final Identifier TEXTURE = Identifier.of("longboatlab", "textures/particle/jet.png");
    private static final double BASE_LENGTH = 4.8;
    private static final double TRAVEL_TICKS = 8.0;
    private static final double HOLD_TICKS = 4.0;
    private static final double TRAIL_TICKS = TRAVEL_TICKS + HOLD_TICKS;
    private record Sample(double time, Vec3d position, Vec3d motion, float yaw, double half, double pitch, double roll, double centerY, int faces) {}
    private static final Map<BoatEntity, ArrayDeque<Sample>> HISTORY = new WeakHashMap<>();
    private static final Map<BoatEntity, PufferJetSound> SOUNDS = new WeakHashMap<>();
    private record Emitters(int age, Vec3d camera, float extension, List<PufferAttachments.Mount> mounts,
                            double[] lengths, double[] widths) {}
    private static final Map<BoatEntity, Emitters> EMITTERS = new WeakHashMap<>();
    private static List<BoatEntity> nearby = List.of();
    private BoatExhaust() {}
    public static void tick(MinecraftClient client) {
        if (client.world == null) {
            SOUNDS.values().forEach(client.getSoundManager()::stop);
            HISTORY.clear(); SOUNDS.clear(); EMITTERS.clear(); nearby = List.of(); return;
        }
        SOUNDS.entrySet().removeIf(entry -> {
            BoatEntity boat = entry.getKey();
            boolean done = entry.getValue().isDone() || boat.isRemoved() || boat.getWorld() != client.world;
            if (done) client.getSoundManager().stop(entry.getValue());
            return done;
        });
        HISTORY.keySet().removeIf(boat -> boat.isRemoved() || boat.getWorld() != client.world
                || HISTORY.get(boat).stream().noneMatch(s -> s.faces != 0 && boat.age - s.time <= TRAIL_TICKS + 1));
        for (BoatEntity boat : LoadedBoats.in(client.world)) {
            BoatAccess access = (BoatAccess) boat;
            boolean firing = access.longboat$visualState().getInt("Boost") > 0;
            if ((!firing && !HISTORY.containsKey(boat)) || access.longboat$puffers() == 0) continue;
            var history = HISTORY.computeIfAbsent(boat, ignored -> new ArrayDeque<>());
            history.addFirst(new Sample(boat.age, boat.getPos(),
                    boat.getPos().subtract(new Vec3d(boat.lastRenderX, boat.lastRenderY, boat.lastRenderZ)), boat.getYaw(),
                    1 + (access.longboat$segments() - 1) * (double) BoatMorph.extension(boat, 1), BoatBodyView.pitch(boat, 1), BoatBodyView.roll(boat, 1), BoatBody.visualCenterY(boat,1), firing ? access.longboat$visualState().getInt("JetFaces") : 0));
            while (history.size() > 16) history.removeLast();
            if (firing && !SOUNDS.containsKey(boat)) {
                var sound = new PufferJetSound(boat);
                SOUNDS.put(boat, sound); client.getSoundManager().play(sound);
            }
        }
        EMITTERS.keySet().removeIf(boat -> !HISTORY.containsKey(boat));
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        nearby = HISTORY.isEmpty() ? List.of() : LoadedBoats.near(client.world,
                new Box(camera.add(-100, -100, -100), camera.add(100, 100, 100)))
                .stream().filter(HISTORY::containsKey).toList();
    }
    private static Sample at(BoatEntity boat, double time, Sample current) {
        var history = HISTORY.get(boat);
        if (time >= current.time) return current;
        if (history == null || history.isEmpty()) return null;
        Sample newer = current;
        for (Sample older : history) {
            if (older.time > time) { if (older.time < newer.time) newer = older; continue; }
            double t = newer.time == older.time ? 1 : (time - older.time) / (newer.time - older.time);
            return new Sample(time, older.position.lerp(newer.position, t), older.motion.lerp(newer.motion, t),
                    older.yaw + MathHelper.wrapDegrees(newer.yaw - older.yaw) * (float) t,
                    MathHelper.lerp(t, older.half, newer.half), MathHelper.lerp(t, older.pitch, newer.pitch), MathHelper.lerp(t, older.roll, newer.roll),
                    MathHelper.lerp(t,older.centerY,newer.centerY),older.faces & newer.faces);
        }
        return null; // Do not invent trails before the recorded emission started.
    }
    public static void renderWorld(WorldRenderContext context, VertexConsumerProvider consumers) {
        var matrices = context.matrixStack();
        if (matrices == null) return;
        Vec3d camera = context.camera().getPos();
        float delta = context.tickCounter().getTickDelta(false);
        // Active and fading emitters are already tracked; no entity-renderer visibility dependency.
        for (BoatEntity boat : nearby) {
            if (!HISTORY.containsKey(boat) || boat.isRemoved() || boat.getWorld() != context.world()) continue;
            Vec3d position = BoatBody.visualPosition(boat, delta).subtract(camera);
            double half = 1 + (((BoatAccess) boat).longboat$segments() - 1) * (double) BoatMorph.extension(boat, delta);
            matrices.push();
            try {
                matrices.translate(position.x, position.y, position.z);
                render(boat, delta, BoatBody.visualYaw(boat, delta), half, matrices, consumers);
            } finally { matrices.pop(); }
        }
    }
    private static void render(BoatEntity boat, float delta, float yaw, double half,
                               MatrixStack matrices, VertexConsumerProvider consumers) {
        var client = MinecraftClient.getInstance();
        boolean firing = ((BoatAccess) boat).longboat$visualState().getInt("Boost") > 0;
        if (client.world == null || (!firing && !HISTORY.containsKey(boat))) return;
        Vec3d position = BoatBody.visualPosition(boat,delta);
        Sample current = new Sample(boat.age - 1 + delta, position,
                boat.getPos().subtract(new Vec3d(boat.lastRenderX, boat.lastRenderY, boat.lastRenderZ)), yaw, half, BoatBodyView.pitch(boat, delta), BoatBodyView.roll(boat, delta),
                BoatBody.visualCenterY(boat,delta),firing ? ((BoatAccess)boat).longboat$visualState().getInt("JetFaces") : 0);
        var camera = client.gameRenderer.getCamera();
        Vec3d localCamera = com.xc.longboatlab.BoatBody.local(boat, camera.getPos());
        float extension = BoatMorph.extension(boat, delta);
        Emitters emitters = EMITTERS.get(boat);
        if (emitters == null || emitters.age != boat.age || emitters.camera.squaredDistanceTo(localCamera) > 1
                || emitters.extension != extension) {
            int[] counts = PufferGrid.counts(boat);
            double[] lengths = new double[PufferGrid.FACES], widths = new double[PufferGrid.FACES];
            for (int face = 0; face < PufferGrid.FACES; face++) {
                double growth = Math.log(Math.max(1, counts[face])) / Math.log(2);
                lengths[face] = BASE_LENGTH * (1 + 0.30 * growth);
                widths[face] = 1 + 0.16 * growth;
            }
            emitters = new Emitters(boat.age, localCamera, extension,
                    PufferAttachments.near(boat, localCamera, 66, extension), lengths, widths);
            EMITTERS.put(boat, emitters);
        }
        var mounts = emitters.mounts;
        double[] lengths = emitters.lengths, widths = emitters.widths;
        var vertices = consumers.getBuffer(BoatEffectRenderPass.layer(TEXTURE, true));
        int beads = switch (client.options.getParticles().getValue()) {
            case MINIMAL -> 6;
            case DECREASED -> 10;
            default -> 14;
        };
        int movingBeads = beads - 2;
        double cycle = (current.time * movingBeads / TRAIL_TICKS) % 1;
        for (int bead = beads - 1; bead >= 0; bead--) {
            boolean nozzleCore=bead<2;
            // Constant-age particles slow into a four-tick terminal cloud, then dissipate.
            double delay = nozzleCore ? 0 : (bead - 2 + cycle) * TRAIL_TICKS / movingBeads;
            double travel = Math.min(1, delay / TRAVEL_TICKS);
            double progress = 1 - Math.pow(1 - travel, 1.3);
            double dwell = Math.max(0, delay - TRAVEL_TICKS) / HOLD_TICKS;
            Sample source = nozzleCore ? current : at(boat, current.time - delay, current);
            if(source==null)continue;
            // One rotation basis per age band, shared by all mouths and faces in that band.
            double angle = Math.toRadians(source.yaw);
            Vec3d xAxis = BoatBody.direction(new Vec3d(1, 0, 0), angle, source.pitch, source.roll);
            Vec3d yAxis = BoatBody.direction(new Vec3d(0, 1, 0), angle, source.pitch, source.roll);
            Vec3d zAxis = BoatBody.direction(new Vec3d(0, 0, 1), angle, source.pitch, source.roll);
            Vec3d[] outwardFaces = {yAxis.multiply(-1), zAxis.multiply(-1), zAxis, xAxis, xAxis.multiply(-1)};
            double fade = Math.min(1, delay / 0.5) * Math.min(1, (TRAIL_TICKS - delay) / 2);
            int alpha = nozzleCore ? 150 : (int) ((128 - 25 * progress - 20 * dwell) * fade);
            for (var mount : mounts) {
                if((source.faces & (1<<mount.face()))==0)continue;
                float size = (float) ((nozzleCore ? 0.22 : 0.28 + progress * 0.42 + dwell * 0.16) * widths[mount.face()]);
                int segments = ((BoatAccess) boat).longboat$segments();
                float sourceExtension = segments <= 1 ? extension : (float) ((source.half - 1) / (segments - 1));
                Vec3d mouth = PufferAttachments.mouth(boat, mount, sourceExtension);
                // Position and direction use the same emission-time frame. The direction is a vector, not a pivoted point.
                Vec3d outward = outwardFaces[mount.face()];
                Vec3d origin = source.position.add(xAxis.multiply(mouth.x)).add(yAxis.multiply(mouth.y - source.centerY))
                        .add(zAxis.multiply(mouth.z)).add(0, source.centerY, 0);
                Vec3d world=origin.add(outward.multiply(nozzleCore?bead*0.18:progress*lengths[mount.face()]))
                        .add(source.motion.multiply(delay));
                if (dwell > 0) {
                    // Spread the terminal cloud instead of stacking several opaque discs at one point.
                    Vec3d side = outward.crossProduct(Math.abs(outward.y) > 0.9 ? new Vec3d(1,0,0) : new Vec3d(0,1,0)).normalize();
                    Vec3d up = outward.crossProduct(side);
                    double phase = source.time * 2.39996 + mount.index() * 0.73 + mount.segment() * 0.37;
                    world = world.add(side.multiply(Math.cos(phase) * dwell * 0.45 * widths[mount.face()]))
                            .add(up.multiply(Math.sin(phase) * dwell * 0.45 * widths[mount.face()]));
                }
                if (world.squaredDistanceTo(camera.getPos()) > 64 * 64) continue;
                Vec3d relative = world.subtract(position);
                matrices.push();
                matrices.translate(relative.x, relative.y, relative.z);
                matrices.multiply(camera.getRotation());
                quad(vertices, matrices.peek(), size, alpha, 0xF000F0);
                matrices.pop();
            }
        }
    }
    private static void quad(VertexConsumer vertices, MatrixStack.Entry entry, float size, int alpha, int light) {
        vertex(vertices, entry, -size, -size, 0, 1, alpha, light);
        vertex(vertices, entry, size, -size, 1, 1, alpha, light);
        vertex(vertices, entry, size, size, 1, 0, alpha, light);
        vertex(vertices, entry, -size, size, 0, 0, alpha, light);
    }
    private static void vertex(VertexConsumer vertices, MatrixStack.Entry entry, float x, float y, float u, float v, int alpha, int light) {
        vertices.vertex(entry.getPositionMatrix(), x, y, 0).color(255, 255, 255, alpha).texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, 0, 1);
    }
}
