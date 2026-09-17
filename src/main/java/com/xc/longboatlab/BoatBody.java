package com.xc.longboatlab;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.*;

/** Server-authoritative pitch/roll dynamics. All forces and collision parts share this local frame. */
public final class BoatBody {
    public static final double GRAVITY = 0.04;
    private record Visual(double oldPitch,double pitch,double oldRoll,double roll, float oldExtension, float extension, float oldSpring, float spring) {}
    private static final java.util.Map<BoatEntity,Visual> VISUALS=java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    public static void sampleVisual(BoatEntity boat) {
        double p=pitch(boat),r=roll(boat);var old=VISUALS.get(boat);
        float extension=BoatGeometry.extension(boat), spring=((BoatAccess)boat).longboat$visualState().getFloat("Spring");
        VISUALS.put(boat,new Visual(old==null?p:old.pitch,p,old==null?r:old.roll,r,
                old==null?extension:old.extension,extension,old==null?spring:old.spring,spring));
    }
    public static void clearVisuals(){VISUALS.clear();}
    public static double visualPitch(BoatEntity boat,float delta){var v=VISUALS.get(boat);return v==null?pitch(boat):v.oldPitch+(v.pitch-v.oldPitch)*delta;}
    public static double visualRoll(BoatEntity boat,float delta){var v=VISUALS.get(boat);return v==null?roll(boat):v.oldRoll+(v.roll-v.oldRoll)*delta;}
    public static float visualExtension(BoatEntity boat,float delta) {
        var v=VISUALS.get(boat);return v==null?BoatGeometry.extension(boat):MathHelper.lerp(delta,v.oldExtension,v.extension);
    }
    public static double visualStackHeight(BoatEntity boat,float delta) {
        var v=VISUALS.get(boat);float spring=v==null?((BoatAccess)boat).longboat$visualState().getFloat("Spring"):MathHelper.lerp(delta,v.oldSpring,v.spring);
        int extra=((BoatAccess)boat).longboat$segments()-1;
        return (1-visualExtension(boat,delta))*(Math.min(0.35,extra*0.035)+extra*spring*0.48);
    }
    public static double visualCenterY(BoatEntity boat,float delta) {return (BoatGeometry.HEIGHT+visualStackHeight(boat,delta))/2;}
    // Network interpolation normalizes yaw at 360 degrees. Always follow the short arc;
    // otherwise the hull, seat and camera sweep a full turn during that one render tick.
    public static float visualYaw(BoatEntity boat,float delta) {return MathHelper.lerpAngleDegrees(delta,boat.prevYaw,boat.getYaw());}
    public static Vec3d visualPosition(BoatEntity boat,float delta) {
        // WorldRenderer uses lastRender*, which can differ from prev* after passenger/network updates.
        return new Vec3d(MathHelper.lerp(delta,boat.lastRenderX,boat.getX()),MathHelper.lerp(delta,boat.lastRenderY,boat.getY()),MathHelper.lerp(delta,boat.lastRenderZ,boat.getZ()));
    }
    public static Vec3d visualOffset(BoatEntity boat,Vec3d local,float yaw,float delta) {
        double center=visualCenterY(boat,delta);
        return direction(local.add(0,-center,0),Math.toRadians(yaw),visualPitch(boat,delta),visualRoll(boat,delta)).add(0,center,0);
    }
    private double pitch, roll, pitchSpeed, rollSpeed, yawImpulse;
    private boolean surfaceSupported;
    private Vec3d lastHorizontalForward;
    private float lastHeadingYaw;
    /** Project the actual rotated bow, not the yaw-only frame that can point aft after a flip. */
    public Vec3d horizontalHeading(BoatEntity boat) {
        Vec3d bow = direction(boat, new Vec3d(0, 0, 1));
        Vec3d flat = new Vec3d(bow.x, 0, bow.z);
        if (flat.lengthSquared() > 1.0e-4) lastHorizontalForward = flat.normalize();
        else if (lastHorizontalForward == null)
            lastHorizontalForward = BoatGeometry.rotateY(new Vec3d(0, 0, 1), Math.toRadians(boat.getYaw()));
        else lastHorizontalForward = BoatGeometry.rotateY(lastHorizontalForward,
                Math.toRadians(MathHelper.wrapDegrees(boat.getYaw() - lastHeadingYaw)));
        lastHeadingYaw = boat.getYaw();
        return lastHorizontalForward;
    }
    /** Rowing steers horizontally even after leaving water; only jets and the spring can supply lift. */
    public Vec3d rowingDirection(BoatEntity boat) {
        return horizontalHeading(boat);
    }
    private static double uprightError(double angle) {return Math.atan2(Math.sin(angle),Math.cos(angle));}
    private static double settleSpeed(double speed,double error) {
        // Correct through the collision solver, never snap the pose through terrain.
        return Math.abs(error)<0.003 && Math.abs(speed)<0.004 ? -error : speed;
    }
    public float takeYawImpulse(){float value=(float)Math.toDegrees(yawImpulse);yawImpulse=0;return value;}
    public static Vec3d unrotate(BoatEntity boat,Vec3d v) {
        v=BoatGeometry.rotateY(v,-Math.toRadians(boat.getYaw()));
        return rotateX(BoatGeometry.rotateZ(v,-roll(boat)),pitch(boat));
    }
    /** Tilt relative to the yaw-only passenger frame. Mouse look remains free in that frame. */
    public static Vec3d riderDirection(BoatEntity boat,Vec3d v,float delta) {
        if(!boat.getWorld().isClient)return direction(boat,BoatGeometry.rotateY(v,-Math.toRadians(boat.getYaw())));
        double yaw=Math.toRadians(visualYaw(boat,delta));
        return direction(BoatGeometry.rotateY(v,-yaw),yaw,visualPitch(boat,delta),visualRoll(boat,delta));
    }
    private void torque(Vec3d localTorque,double mass,double half,double width) {
        pitchSpeed-=localTorque.x/Math.max(0.35,mass*(half*half+0.30)/3);
        rollSpeed+=localTorque.z/Math.max(0.35,mass*(0.48+(width*width-BoatGeometry.HALF_WIDTH*BoatGeometry.HALF_WIDTH)/3));
        yawImpulse-=localTorque.y/Math.max(0.5,mass*(half*half+0.48+width*width-BoatGeometry.HALF_WIDTH*BoatGeometry.HALF_WIDTH)/3);
    }

    public double pitch() { return pitch; }
    public double roll() { return roll; }
    public static double pitch(BoatEntity boat) {
        BoatAccess a = (BoatAccess) boat;
        return boat.getWorld().isClient ? a.longboat$visualState().getDouble("TiltPitch")
                : a.longboat$abilities() == null ? 0 : a.longboat$abilities().body().pitch;
    }
    public static double roll(BoatEntity boat) {
        BoatAccess a = (BoatAccess) boat;
        return boat.getWorld().isClient ? a.longboat$visualState().getDouble("TiltRoll")
                : a.longboat$abilities() == null ? 0 : a.longboat$abilities().body().roll;
    }
    public static boolean tilted(BoatEntity boat) {
        return Math.abs(uprightError(pitch(boat))) + Math.abs(uprightError(roll(boat))) > 1e-5;
    }
    public static double centerY(BoatEntity boat) { return (BoatGeometry.HEIGHT + BoatGeometry.stackHeight(boat)) / 2; }
    public static Vec3d rotateX(Vec3d v, double radians) {
        double c = Math.cos(radians), s = Math.sin(radians);
        return new Vec3d(v.x, v.y * c - v.z * s, v.y * s + v.z * c);
    }
    public static Vec3d direction(Vec3d v, double yaw, double pitch, double roll) {
        return BoatGeometry.rotateY(BoatGeometry.rotateZ(rotateX(v, -pitch), roll), yaw);
    }
    public static Vec3d direction(BoatEntity boat, Vec3d v) {
        return direction(v, Math.toRadians(boat.getYaw()), pitch(boat), roll(boat));
    }
    public static Vec3d offset(BoatEntity boat, Vec3d local, float yaw) {
        double center = centerY(boat);
        return direction(local.add(0, -center, 0), Math.toRadians(yaw), pitch(boat), roll(boat)).add(0, center, 0);
    }
    public static Vec3d world(BoatEntity boat, Vec3d local) { return boat.getPos().add(offset(boat, local, boat.getYaw())); }
    public static Vec3d local(BoatEntity boat, Vec3d world) {
        Vec3d v = world.subtract(boat.getPos()).add(0, -centerY(boat), 0);
        v = BoatGeometry.rotateY(v, -Math.toRadians(boat.getYaw()));
        v = BoatGeometry.rotateZ(v, -roll(boat));
        return rotateX(v, pitch(boat)).add(0, centerY(boat), 0);
    }
    public static Box box(BoatEntity boat, Box local, float yaw) {
        Vec3d center = boat.getPos().add(offset(boat, local.getCenter(), yaw));
        double p = pitch(boat), r = roll(boat), y = Math.toRadians(yaw);
        Vec3d x = direction(new Vec3d((local.maxX-local.minX)/2, 0, 0), y, p, r);
        Vec3d up = direction(new Vec3d(0, (local.maxY-local.minY)/2, 0), y, p, r);
        Vec3d z = direction(new Vec3d(0, 0, (local.maxZ-local.minZ)/2), y, p, r);
        double ex = Math.abs(x.x)+Math.abs(up.x)+Math.abs(z.x);
        double ey = Math.abs(x.y)+Math.abs(up.y)+Math.abs(z.y);
        double ez = Math.abs(x.z)+Math.abs(up.z)+Math.abs(z.z);
        return new Box(center.x-ex, center.y-ey, center.z-ez, center.x+ex, center.y+ey, center.z+ez);
    }
    public void load(NbtCompound tag) {
        pitch = finite(tag.getDouble("Pitch")); roll = finite(tag.getDouble("Roll"));
        pitchSpeed = MathHelper.clamp(finite(tag.getDouble("PitchSpeed")), -0.18, 0.18);
        rollSpeed = MathHelper.clamp(finite(tag.getDouble("RollSpeed")), -0.18, 0.18);
    }
    private static double finite(double n) { return Double.isFinite(n) ? n : 0; }
    public NbtCompound save() {
        NbtCompound tag = new NbtCompound(); tag.putDouble("Pitch", pitch); tag.putDouble("Roll", roll);
        tag.putDouble("PitchSpeed", pitchSpeed); tag.putDouble("RollSpeed", rollSpeed); return tag;
    }
    public void setAngles(double pitchDegrees, double rollDegrees) {
        pitch = Math.toRadians(pitchDegrees); roll = Math.toRadians(rollDegrees); pitchSpeed = rollSpeed = 0;
    }
    public void stop() { pitchSpeed = rollSpeed = 0; }
    public void resetForRescue() {
        pitch = roll = pitchSpeed = rollSpeed = yawImpulse = 0;
        surfaceSupported = false;
        lastHorizontalForward = null;
    }
    private static boolean sweptClear(BoatCollisionScene scene, java.util.List<Box> before, java.util.List<Box> after, Vec3d shift, double margin) {
        for (int j = 0; j < after.size(); j++) {
            Box target = after.get(j).offset(shift);
            Box sweep = before.size() == after.size() ? before.get(j).union(target) : target.expand(margin);
            if (!scene.clear(sweep)) return false;
        }
        return true;
    }
    /** Sample the actual hull rather than a broad box that can still touch water during flight. */
    static double jumpSupport(BoatEntity boat) { return immersion(boat, 0.08); }
    private static double immersion(BoatEntity boat) { return immersion(boat, BoatGeometry.HEIGHT / 2); }
    private static double immersion(BoatEntity boat, double localY) {
        double submerged = 0;
        int samples = 0, along = Math.min(16, Math.max(2, (int) Math.min(16, ((BoatAccess) boat).longboat$segments()*2L)));
        double half = BoatGeometry.halfLength(boat) - 0.2;
        for (int i = 0; i < along; i++) for (double x : new double[] {-0.4, 0.4}) {
            Vec3d probe = world(boat, new Vec3d(x*((BoatAccess)boat).longboat$width(), localY, -half + 2*half*(i+0.5)/along));
            submerged += BoatEnvironment.immersionAt(boat, probe, 0.2, 0.4);
            samples++;
        }
        return submerged / Math.max(1, samples);
    }
    /** Tilted, jumping and submerged hulls share bounded buoyancy independent of total water depth. */
    public void environment(BoatEntity boat) {
        double submerged = immersion(boat);
        Vec3d v = boat.getVelocity();
        double damping = submerged > 0 ? 0.90 : boat.isOnGround() ? 0.90 : 0.99;
        boat.setVelocity(v.x*damping, v.y*(submerged > 0 ? 0.88 : 0.99) - GRAVITY + submerged*0.075, v.z*damping);
    }
    public void step(BoatEntity boat, int jets, boolean anchored) {
        if (anchored) { stop(); yawImpulse=0; surfaceSupported=false; return; }
        BoatAccess a = (BoatAccess) boat;
        double hullMass=1+(a.longboat$segments()*(double)a.longboat$width()-1)*0.30, mass=hullMass;
        double half=BoatGeometry.halfLength(boat);
        // A low keel carries most of the weight; riders influence trim without dominating a short hull.
        Vec3d center=new Vec3d(0,0.10+BoatGeometry.stackHeight(boat)*0.25,0).multiply(hullMass);
        for(var rider:boat.getPassengerList()) {
            center=center.add(BoatSeats.localFeet(boat,rider).add(0,rider.getHeight()*0.45,0).multiply(0.45));mass+=0.45;
        }
        center=center.multiply(1/mass);
        int[] counts=PufferGrid.counts(boat); Vec3d[] centers=PufferGrid.centers(boat);
        for(int face=0;face<5;face++)if((jets&(1<<face))!=0 && counts[face]>0) {
            // Preserve the established horizontal boost on every face. Its arcade speed bonus
            // must not become a gravity-defeating upward acceleration when the hull pitches/rolls.
            double horizontalAcceleration=face==PufferGrid.BOTTOM ? Math.min(0.075,0.018*Math.sqrt(counts[face])/Math.sqrt(mass))
                    : Math.min(0.45,(0.075+0.035*Math.sqrt(counts[face]))/Math.sqrt(mass));
            // All faces share the same vertical force budget, divided by the entire loaded mass.
            // Gravity remains a separate world-down acceleration in environment()/vanilla physics.
            double verticalAcceleration=Math.min(0.075,0.018*Math.sqrt(counts[face])/mass);
            Vec3d axis=direction(boat,PufferGrid.normal(face).negate());
            Vec3d acceleration=new Vec3d(axis.x*horizontalAcceleration,axis.y*verticalAcceleration,
                    axis.z*horizontalAcceleration);
            boat.setVelocity(boat.getVelocity().add(acceleration));
            // Rotation must use the actual applied force, including the reduced vertical component.
            Vec3d force=unrotate(boat,acceleration.multiply(mass));
            torque(centers[face].subtract(center).crossProduct(force),mass,half,BoatGeometry.halfWidth(boat));
        }
        double drivenPitchSpeed=pitchSpeed, drivenRollSpeed=rollSpeed;
        // Uniform gravity has no torque in free fall. Ground support and buoyancy act away from the mass center.
        var scene=BoatCollisionScene.of(boat); Vec3d support=Vec3d.ZERO; int contacts=0;
        double minSupportX=Double.POSITIVE_INFINITY,maxSupportX=Double.NEGATIVE_INFINITY;
        double minSupportZ=Double.POSITIVE_INFINITY,maxSupportZ=Double.NEGATIVE_INFINITY;
        // Riders remain in motion collisions, but do not act as extra feet holding the boat in a tilted pose.
        var hull=BoatGeometry.structureParts(boat,boat.getYaw()); int probes=Math.min(32,hull.size());
        for(int i=0;i<probes;i++) {
            int index=probes==1?0:(int)((long)i*(hull.size()-1)/(probes-1));
            Box part=hull.get(index); Vec3d clipped=scene.clip(part,new Vec3d(0,-0.09,0));
            if(clipped.y> -0.089 && !scene.unknown()) {
                support=support.add(new Vec3d((part.minX+part.maxX)/2,part.minY,(part.minZ+part.maxZ)/2));contacts++;
                minSupportX=Math.min(minSupportX,part.minX);maxSupportX=Math.max(maxSupportX,part.maxX);
                minSupportZ=Math.min(minSupportZ,part.minZ);maxSupportZ=Math.max(maxSupportZ,part.maxZ);
            }
        }
        if(contacts>0) {
            Vec3d com=world(boat,center);
            // A broad support patch balances gravity while the center of mass projects inside it.
            Vec3d contact=new Vec3d(MathHelper.clamp(com.x,minSupportX,maxSupportX),support.y/contacts,
                    MathHelper.clamp(com.z,minSupportZ,maxSupportZ));
            support=contact.multiply(contacts);
            Vec3d arm=com.subtract(contact);
            torque(unrotate(boat,arm.crossProduct(new Vec3d(0,-GRAVITY*mass,0))),mass,half,BoatGeometry.halfWidth(boat));
        }
        int along=Math.min(16,Math.max(2,a.longboat$segments())); double wet=0;
        for(int i=0;i<along;i++)for(double x:new double[]{-0.42,0.42}) {
            Vec3d local=new Vec3d(x*a.longboat$width(),0.10,(-half+0.2)+(2*half-0.4)*(i+0.5)/along);
            Vec3d probe=world(boat,local);
            double immersion=BoatEnvironment.immersionAt(boat,probe,0,0.45);
            if(immersion<=0)continue;
            wet+=immersion;
            Vec3d up=unrotate(boat,new Vec3d(0,immersion*mass*0.075/(along*2),0));
            torque(local.subtract(center).crossProduct(up),mass,half,BoatGeometry.halfWidth(boat));
        }
        surfaceSupported=contacts>0 || wet>0;
        double pitchError=uprightError(pitch), rollError=uprightError(roll);
        double up=direction(boat,new Vec3d(0,1,0)).y;
        boolean settling=jets==0 && surfaceSupported && boat.getVelocity().y<=0.12
                && Math.abs(pitchError)<Math.toRadians(65) && Math.abs(rollError)<Math.toRadians(65)
                && up>Math.cos(Math.toRadians(65));
        if(settling) {
            // The coarse buoyancy/contact samples can leave a constant trim torque with an off-center rider.
            // Blend that torque down near upright, then damp toward a stable sailing attitude.
            double passiveWeight=0.15;
            double spring=contacts>0?0.035:0.025;
            pitchSpeed=drivenPitchSpeed+(pitchSpeed-drivenPitchSpeed)*passiveWeight-pitchError*spring;
            rollSpeed=drivenRollSpeed+(rollSpeed-drivenRollSpeed)*passiveWeight-rollError*spring;
        }
        double damping=contacts>0?(jets==0?0.76:0.94):wet>0?(jets==0?0.86:0.97):0.998;
        pitchSpeed=MathHelper.clamp(pitchSpeed*damping,-0.18,0.18);
        rollSpeed=MathHelper.clamp(rollSpeed*damping,-0.18,0.18);
        if(settling) {
            pitchSpeed=settleSpeed(pitchSpeed,pitchError);
            rollSpeed=settleSpeed(rollSpeed,rollError);
        }
        if (Math.abs(pitchSpeed) + Math.abs(rollSpeed) < 1e-6) { stop(); return; }
        // Limit tip displacement per physics step for long hulls; short boats may perform complete somersaults.
        double radius = Math.max(Math.hypot(half,BoatGeometry.halfWidth(boat)), GiantOars.mounted(boat).stream().mapToDouble(m -> m.scale()*1.8+BoatGeometry.halfWidth(boat)).max().orElse(1));
        double magnitude = Math.abs(pitchSpeed) + Math.abs(rollSpeed);
        double fraction = Math.min(1, 0.9 / (radius * magnitude));
        double dp = pitchSpeed * fraction / 6, dr = rollSpeed * fraction / 6;
        double liftUsed=0, liftBudget=jets==0?0.06:0.16;
        for (int i = 0; i < 6; i++) {
            double oldPitch = pitch, oldRoll = roll;
            Vec3d oldPosition = boat.getPos();
            double priorLift=liftUsed;
            var before = BoatGeometry.collisionParts(boat, boat.getYaw());
            Vec3d pivotWorld=contacts>0?support.multiply(1.0/contacts):world(boat,center);
            Vec3d pivotLocal=local(boat,pivotWorld);
            pitch += dp; roll += dr;
            boat.setPosition(boat.getPos().add(pivotWorld.subtract(world(boat,pivotLocal))));
            var after = BoatGeometry.collisionParts(boat, boat.getYaw());

            boolean clear = sweptClear(scene, before, after, Vec3d.ZERO, radius*(Math.abs(dp)+Math.abs(dr)));
            if (!clear && contacts>0) {
                // Resolve a ground pivot by a small positional lift, without adding upward velocity.
                for (double lift : new double[] {0.01, 0.02, 0.04, 0.08, 0.16}) {
                    if(liftUsed+lift>liftBudget+1e-8)continue;
                    Vec3d correction = new Vec3d(0, lift, 0);
                    if (sweptClear(scene, before, after, correction, radius*(Math.abs(dp)+Math.abs(dr)))) {
                        boat.setPosition(boat.getPos().add(correction)); liftUsed+=lift; clear = true; break;
                    }
                }
            }
            if (!clear) {
                pitch=oldPitch; roll=oldRoll; boat.setPosition(oldPosition);liftUsed=priorLift;
                // Dissipate only the blocked motion. Preserve the other angular component if it has clearance.
                pitch+=dp;boat.setPosition(oldPosition.add(pivotWorld.subtract(world(boat,pivotLocal))));
                if(sweptClear(scene,before,BoatGeometry.collisionParts(boat,boat.getYaw()),Vec3d.ZERO,0)) {
                    rollSpeed*=0.15; dr=0;
                } else {
                    pitch=oldPitch;roll+=dr;boat.setPosition(oldPosition);boat.setPosition(oldPosition.add(pivotWorld.subtract(world(boat,pivotLocal))));
                    if(sweptClear(scene,before,BoatGeometry.collisionParts(boat,boat.getYaw()),Vec3d.ZERO,0)) {pitchSpeed*=0.15;dp=0;}
                    else {pitch=oldPitch;roll=oldRoll;boat.setPosition(oldPosition);pitchSpeed*=0.15;rollSpeed*=0.15;break;}
                }
            }
        }
        if(settling && contacts>0 && boat.getVelocity().y<=0 && liftUsed>0) {
            // Take up only the clearance introduced by this tick's correction; do not leave the hull floating.
            Vec3d settle=BoatGeometry.clipMovement(boat,new Vec3d(0,-liftUsed,0));
            boat.setPosition(boat.getPos().add(settle));
        }
        boat.setBoundingBox(BoatGeometry.bounds(boat));
        LoadedBoats.update(boat);
    }
}
