package com.xc.longboatlab.client.water;

import com.xc.longboatlab.BoatBody;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.LongboatLab;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/** Client-local contact audio: one wave loop per hull and one slap per impact group. */
final class BoatWaterSounds {
    private static final Map<BoatEntity, Wake> WAKES = new WeakHashMap<>();
    private static final Map<UUID, Integer> NEXT_IMPACT = new HashMap<>();
    private static ClientWorld world;
    private static int clock, impactBudget;
    private BoatWaterSounds() {}

    static void tick(MinecraftClient client) {
        if (world != client.world) {
            WAKES.values().forEach(client.getSoundManager()::stop);
            WAKES.clear(); NEXT_IMPACT.clear(); world = client.world; clock = 0;
        }
        clock++; impactBudget = 4;
        WAKES.entrySet().removeIf(e -> {
            boolean done = e.getValue().isDone() || e.getKey().isRemoved();
            if (done) client.getSoundManager().stop(e.getValue());
            return done;
        });
        if (clock % 100 == 0) NEXT_IMPACT.values().removeIf(t -> t < clock);
    }

    static void wake(MinecraftClient client, BoatEntity boat, double speed, boolean touching) {
        double power = touching ? MathHelper.clamp((speed - 0.09) / 0.72, 0, 1) : 0;
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        Vec3d local = BoatBody.local(boat, camera);
        Vec3d edge = BoatBody.world(boat, new Vec3d(Math.copySign(BoatGeometry.halfWidth(boat), local.x),
                0.18, MathHelper.clamp(local.z, -BoatGeometry.halfLength(boat), BoatGeometry.halfLength(boat))));
        if (edge.squaredDistanceTo(camera) > 48 * 48) power = 0;
        Wake sound = WAKES.get(boat);
        if (power > 0 && sound == null) {
            sound = new Wake(boat, edge); WAKES.put(boat, sound);
            sound.update(edge, power); client.getSoundManager().play(sound);
        } else if (sound != null) sound.update(edge, power);
    }

    static void impact(MinecraftClient client, WaterImpact impact) {
        if (impact.kind() != WaterImpact.Kind.ENTRY || impactBudget <= 0
                || NEXT_IMPACT.getOrDefault(impact.boat(), 0) > clock) return;
        Vec3d at = impact.position();
        if (at.squaredDistanceTo(client.gameRenderer.getCamera().getPos()) > 64 * 64) return;
        NEXT_IMPACT.put(impact.boat(), clock + 8); impactBudget--;
        float strength = (float)MathHelper.clamp(impact.normalSpeed() * 1.8 + Math.sqrt(impact.area()) * 0.18, 0, 1.5);
        float volume = 1.15f + strength * 0.75f;
        float pitch = 1.06f - strength * 0.14f + client.world.random.nextFloat() * 0.06f;
        client.getSoundManager().play(new PositionedSoundInstance(LongboatLab.WATER_IMPACT, SoundCategory.BLOCKS,
                volume, pitch, Random.create(), at.x, at.y, at.z));
        // Native splash detail sits over the lower body slap / receding-water layer.
        client.getSoundManager().play(new PositionedSoundInstance(SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED,
                SoundCategory.BLOCKS, 0.65f + strength * 0.35f, pitch, Random.create(), at.x, at.y, at.z));
    }

    private static final class Wake extends MovingSoundInstance {
        private final BoatEntity boat;
        private int refreshed;
        private float target;
        Wake(BoatEntity boat, Vec3d at) {
            super(LongboatLab.WATER_WAKE, SoundCategory.BLOCKS, Random.create());
            this.boat = boat; repeat = true; repeatDelay = 0; volume = 0.04f; pitch = 1;
            x = at.x; y = at.y; z = at.z;
        }
        void update(Vec3d at, double power) {
            refreshed = clock; x = at.x; y = at.y; z = at.z;
            target = power <= 0 ? 0 : (float)(0.38 + power * 0.82);
            pitch = (float)(0.87 + power * 0.18);
        }
        @Override public void tick() {
            if (boat.isRemoved() || boat.getWorld() != MinecraftClient.getInstance().world) { setDone(); return; }
            if (clock - refreshed > 2) target = 0;
            volume += (target - volume) * (target > volume ? 0.22f : 0.13f);
            if (target == 0 && volume < 0.008f) setDone();
        }
    }
}
