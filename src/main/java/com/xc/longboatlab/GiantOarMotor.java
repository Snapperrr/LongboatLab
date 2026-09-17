package com.xc.longboatlab;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.*;

/** A bounded joint solver with sticky feet, not an impulse multiplier proportional to paddle size. */
public final class GiantOarMotor {
    private static final int SUBSTEPS = 4;
    private double left, right, nextLeft, nextRight;
    private boolean pending;
    public double phase(boolean port) { return port ? left : right; }
    public NbtCompound save() {
        NbtCompound tag = new NbtCompound(); tag.putDouble("Left", left); tag.putDouble("Right", right); return tag;
    }
    public void load(NbtCompound tag) {
        left = Double.isFinite(tag.getDouble("Left")) ? tag.getDouble("Left") : 0;
        right = Double.isFinite(tag.getDouble("Right")) ? tag.getDouble("Right") : 0;
        pending = false;
    }
    public void drive(BoatEntity boat, boolean forward, boolean back, int steering, Vec3d heading) {
        pending = false;
        var mounts = GiantOars.mounted(boat);
        if (mounts.isEmpty()) return;
        double throttle = (forward ? 1 : 0) - (back ? 1 : 0);
        Vec3d motion = boat.getVelocity();
        if (throttle == 0 && steering == 0 && motion.x * motion.x + motion.z * motion.z < 1.0e-8) return;
        nextLeft = left + MathHelper.clamp(throttle + steering, -1, 1) * Math.PI / 8;
        nextRight = right + MathHelper.clamp(throttle - steering, -1, 1) * Math.PI / 8;
        var scene = BoatCollisionScene.of(boat);
        int grounded = 0;
        double size = 1;
        for (var mount : mounts) {
            double phase = phase(mount.left());
            var boxes = GiantOars.boxes(boat, mount, phase, boat.getYaw());
            // Probe only three blade sections for traction. Full shaft/blade boxes still collide during motion.
            boolean ground = false;
            for (int i = Math.max(0, boxes.size() - 3); i < boxes.size(); i++) {
                Box box = boxes.get(i);
                Vec3d drop = new Vec3d(0, -0.07, 0);
                if (scene.clip(box, drop).y > -0.069 && !scene.unknown()) { ground = true; break; }
            }
            if (!ground) continue;
            grounded++;
            size = Math.max(size, mount.scale());
        }
        if (grounded > 0 && !boat.isTouchingWater()) {
            Vec3d v = boat.getVelocity();
            // Damped horizontal traction. Ground support is supplied by collision, not a fresh jump every stroke.
            double speed = throttle * Math.min(0.38, 0.10 + Math.sqrt(size) * 0.055);
            Vec3d desired = heading.multiply(speed);
            double grip = 0.50;
            Vec3d traction = new Vec3d((desired.x - v.x) * grip, 0, (desired.z - v.z) * grip);
            if (traction.length() > 0.12) traction = traction.normalize().multiply(0.12);
            // Keep gravity and deliberate jet/jump impulses; never generate vertical speed merely by rowing.
            boat.setVelocity(v.x + traction.x, v.y, v.z + traction.z);
        }
        pending = true;
    }
    public void afterMove(BoatEntity boat) {
        if (!pending) return;
        pending = false;
        left = advance(boat, true, left, nextLeft);
        right = advance(boat, false, right, nextRight);
    }
    private double advance(BoatEntity boat, boolean port, double before, double after) {
        if (before == after) return before;
        double accepted = before;
        for (int step = 1; step <= SUBSTEPS; step++) {
            double candidate = before + (after - before) * step / SUBSTEPS;
            if (!clearArc(boat, port, accepted, candidate)) {
                // Constant-cost bisection retains small useful strokes while the foot is planted.
                double low = accepted, high = candidate;
                for (int i = 0; i < 3; i++) {
                    double middle = (low + high) * 0.5;
                    if (clearArc(boat, port, accepted, middle)) low = middle; else high = middle;
                }
                return low;
            }
            accepted = candidate;
        }
        return accepted;
    }
    private boolean clearArc(BoatEntity boat, boolean port, double from, double to) {
        var scene = BoatCollisionScene.of(boat);
        for (var mount : GiantOars.mounted(boat)) {
            if (mount.left() != port) continue;
            var old = GiantOars.boxes(boat, mount, from, boat.getYaw());
            var next = GiantOars.boxes(boat, mount, to, boat.getYaw());
            // Bound the curved path between endpoint boxes, preserving collision at large sizes.
            double arcMargin = mount.scale() * (to - from) * (to - from) * 0.32;
            for (int i = 0; i < old.size(); i++) {
                Box sweep = old.get(i).union(next.get(i)).expand(arcMargin);
                if (!scene.clear(sweep)) return false;
            }
        }
        return true;
    }
}
