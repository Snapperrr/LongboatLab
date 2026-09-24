package com.xc.longboatlab;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.*;

/** Grounded paddle drive with bounded, collision-checked foot and hull movement. */
public final class GiantOarMotor {
    private static final int SUBSTEPS = 4;
    private double left, right, nextLeft, nextRight;
    private double climbHeight, liftRemaining;
    private long driveTick = Long.MIN_VALUE;
    private boolean pending;
    public double phase(boolean port) { return port ? left : right; }
    public NbtCompound save() {
        NbtCompound tag = new NbtCompound(); tag.putDouble("Left", left); tag.putDouble("Right", right); return tag;
    }
    public void load(NbtCompound tag) {
        left = Double.isFinite(tag.getDouble("Left")) ? tag.getDouble("Left") : 0;
        right = Double.isFinite(tag.getDouble("Right")) ? tag.getDouble("Right") : 0;
        pending = false; climbHeight = liftRemaining = 0; driveTick = Long.MIN_VALUE;
    }
    public double stepHeight(BoatEntity boat) {
        return pending && driveTick == boat.getWorld().getTime() && boat.getVelocity().y <= 0.18 ? climbHeight : 0;
    }
    public void drive(BoatEntity boat, boolean forward, boolean back, int steering, Vec3d heading) {
        pending = false; climbHeight = liftRemaining = 0;
        driveTick = boat.getWorld().getTime();
        var mounts = GiantOars.mounted(boat);
        if (mounts.isEmpty()) return;
        double throttle = (forward ? 1 : 0) - (back ? 1 : 0);
        if (throttle == 0 && steering == 0) return;
        // Keep vanilla's stroke angular speed. Larger blades gain area and reach,
        // not faster animation or a size-proportional launch impulse.
        nextLeft = left + MathHelper.clamp(throttle + steering, -1, 1) * Math.PI / 8;
        nextRight = right + MathHelper.clamp(throttle - steering, -1, 1) * Math.PI / 8;
        var scene = BoatCollisionScene.of(boat);
        double area = 0, size = 1;
        for (var mount : mounts) {
            boolean active = mount.left() ? nextLeft != left : nextRight != right;
            if (!active) continue;
            var boxes = GiantOars.boxes(boat, mount, phase(mount.left()), boat.getYaw());
            // Three blade probes per mount, independent of blade size. A wall
            // contact alone is not support: an airborne boat cannot climb air.
            for (int i = Math.max(0, boxes.size() - 3); i < boxes.size(); i++) {
                Vec3d drop = scene.clip(boxes.get(i), new Vec3d(0, -0.12, 0));
                if (drop.y > -0.119 && !scene.unknown()) {
                    area += mount.scale() * mount.scale();
                    size = Math.max(size, mount.scale());
                    break;
                }
            }
        }
        if (area > 0) {
            BoatAccess access = (BoatAccess) boat;
            double mass = GiantOarPhysics.mass(access.longboat$segments(), access.longboat$width(), boat.getPassengerList().size());
            liftRemaining = GiantOarPhysics.jointLift(size, area, mass);
            if (throttle != 0) {
                climbHeight = GiantOarPhysics.stepHeight(size, area, mass);
                Vec3d v = boat.getVelocity(), direction = heading.multiply(throttle);
                double along = v.dotProduct(direction);
                double speed = GiantOarPhysics.speed(area, mass) * (back ? 0.65 : 1);
                double force = GiantOarPhysics.acceleration(area, mass);
                // Grip accelerates toward stroke speed, but does not brake
                // stronger puffer thrust or replace existing vertical speed.
                double push = Math.min(force, Math.max(0, speed - along) * 0.72);
                Vec3d sideways = new Vec3d(v.x, 0, v.z).subtract(direction.multiply(along));
                double slip = sideways.length();
                if (slip > 1e-8) sideways = sideways.multiply(Math.min(0.20, force * 0.25 / slip));
                boat.setVelocity(v.add(direction.multiply(push)).subtract(sideways));
            }
        }
        pending = true;
    }
    public void afterMove(BoatEntity boat) {
        if (!pending) return;
        pending = false;
        advance(boat, true, left, nextLeft);
        advance(boat, false, right, nextRight);
        boat.setBoundingBox(BoatGeometry.bounds(boat));
    }
    private void advance(BoatEntity boat, boolean port, double before, double after) {
        if (before == after) return;
        double accepted = before;
        for (int step = 1; step <= SUBSTEPS; step++) {
            double candidate = before + (after - before) * step / SUBSTEPS;
            if (!moveJoint(boat, port, accepted, candidate)) {
                // Fixed-cost bisection retains useful motion at a planted foot.
                double high = candidate;
                for (int i = 0; i < 3; i++) {
                    double middle = (accepted + high) * 0.5;
                    if (moveJoint(boat, port, accepted, middle)) accepted = middle; else high = middle;
                }
                return;
            }
            accepted = candidate;
        }
    }
    private boolean moveJoint(BoatEntity boat, boolean port, double from, double to) {
        var scene = BoatCollisionScene.of(boat);
        List<Box> sweeps = new ArrayList<>();
        double rise = 0;
        boolean needsLift = false;
        for (var mount : GiantOars.mounted(boat)) {
            if (mount.left() != port) continue;
            var old = GiantOars.boxes(boat, mount, from, boat.getYaw());
            var next = GiantOars.boxes(boat, mount, to, boat.getYaw());
            double margin = mount.scale() * (to - from) * (to - from) * 0.32;
            for (int i = 0; i < old.size(); i++) {
                Box sweep = old.get(i).union(next.get(i)).expand(margin);
                sweeps.add(sweep);
                if (scene.clear(sweep)) continue;
                if (scene.unknown() || liftRemaining <= 1e-6 || boat.getVelocity().y > 0.18) return false;
                double needed = scene.riseToClear(sweep, liftRemaining);
                if (!Double.isFinite(needed)) return false;
                rise = Math.max(rise, needed);
                needsLift = true;
            }
        }
        if (needsLift) {
            rise += 1e-5;
            if (rise > liftRemaining) return false;
            Vec3d lift = new Vec3d(0, rise, 0);
            // Full hull, other oars and passengers must fit through the lift.
            if (BoatGeometry.clipMovement(boat, lift).squaredDistanceTo(lift) > 1e-10) return false;
            for (Box sweep : sweeps) if (!scene.clear(sweep.offset(lift))) return false;
            boat.setPosition(boat.getPos().add(lift));
            liftRemaining -= rise;
        }
        if (port) left = to; else right = to;
        if (needsLift) {
            // Remove spare arc clearance using the NEW joint pose. Positional
            // support never becomes upward velocity that can accumulate in air.
            Vec3d settle = BoatGeometry.clipMovement(boat, new Vec3d(0, -rise, 0));
            boat.setPosition(boat.getPos().add(settle));
        }
        return true;
    }
}
