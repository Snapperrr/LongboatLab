package com.xc.longboatlab;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Hull coordinates: +X = port/left, +Z = bow, +Y = up; units are blocks. */
public final class BoatGeometry {
    public static final double HALF_WIDTH = 11.0 / 16;
    public static final double HEIGHT = 9.0 / 16;
    private static final Box SHAFT = new Box(-0.25, -0.055, -0.055, 1.05, 0.055, 0.055);
    private static final Box BLADE = new Box(0.75, -0.075, -0.15, 1.25, 0.075, 0.15);

    private BoatGeometry() {}
    private record CachedParts(Vec3d position, float yaw, double length, double width, double stack, double pitch, double roll, List<Box> boxes) {}
    private static final ThreadLocal<java.util.Map<BoatEntity, CachedParts>> PARTS =
            ThreadLocal.withInitial(java.util.WeakHashMap::new);
    public record Target(BoatEntity boat, boolean left, int oar, double distanceSquared, Vec3d localHit, PufferAttachments.Mount puffer) {
        public Target(BoatEntity boat, boolean left, int oar, double distanceSquared, Vec3d localHit) { this(boat, left, oar, distanceSquared, localHit, null); }
    }
    public record OarPose(Vec3d pivot, float yaw, float dip, int sign) {}

    public static double halfWidth(BoatEntity boat) { return HALF_WIDTH * ((BoatAccess)boat).longboat$width(); }
    public static double laneX(int lane, int width) { return (lane - (width - 1) / 2.0) * HALF_WIDTH * 2; }
    public static int laneAt(BoatEntity boat, double x) {
        int width=((BoatAccess)boat).longboat$width();
        return net.minecraft.util.math.MathHelper.clamp((int)Math.floor((x+halfWidth(boat))/(HALF_WIDTH*2)),0,width-1);
    }
    public static int visibleSegments(BoatEntity boat) {
        BoatAccess access = (BoatAccess) boat;
        return access.longboat$compressed() && access.longboat$visualState().getInt("MorphTicks") == 0 ? 1 : access.longboat$segments();
    }
    public static int visibleOars(BoatEntity boat, boolean left) {
        BoatAccess access = (BoatAccess) boat;
        return access.longboat$compressed() ? Math.min(1, access.longboat$oars(left)) : Math.min(256, access.longboat$oars(left));
    }
    public static double halfLength(BoatEntity boat) {
        BoatAccess access = (BoatAccess) boat;
        return access.longboat$visualState().getInt("MorphTicks") > 0 ? access.longboat$segments() : visibleSegments(boat);
    }
    public static Box bounds(BoatEntity boat) { return bounds(boat, boat.getYaw()); }
    public static Box bounds(BoatEntity boat, float yaw) {
        // Vanilla uses this envelope for entity reach checks, including attacks. It must contain
        // the tilted bow/stern too; physical collision still uses the narrower hullParts below.
        return BoatBody.box(boat, new Box(-halfWidth(boat), 0, -halfLength(boat),
                halfWidth(boat), HEIGHT + stackHeight(boat), halfLength(boat)), yaw);
    }

    /** Broad bounds are for discovery only. Collision follows short sections of the hull, not its empty corners. */
    public static List<Box> collisionParts(BoatEntity boat, float yaw) {
        var structure=structureParts(boat,yaw);
        if(!boat.hasPassengers())return structure;
        var parts=new ArrayList<>(structure);parts.addAll(BoatSeats.collisionParts(boat,yaw));return parts;
    }
    public static List<Box> structureParts(BoatEntity boat, float yaw) {
        List<Box> hull = hullParts(boat, yaw);
        if (GiantOars.mounted(boat).isEmpty()) return hull;
        List<Box> all = new ArrayList<>(hull); all.addAll(GiantOars.boxes(boat, yaw)); return all;
    }
    public static List<Box> hullParts(BoatEntity boat, float yaw) {
        double length = halfLength(boat), stack = stackHeight(boat);
        CachedParts cached = PARTS.get().get(boat);
        if (cached != null && cached.position.equals(boat.getPos()) && cached.yaw == yaw
                && cached.length == length && cached.width == halfWidth(boat) && cached.stack == stack
                && cached.pitch == BoatBody.pitch(boat) && cached.roll == BoatBody.roll(boat)) return cached.boxes;
        List<Box> parts = new ArrayList<>();
        // Tile both axes. Scaling a single AABB sideways produces phantom diagonal corners.
        // The shared budget is independent of command dimensions.
        for (int tier = 0; tier < 2; tier++) {
            double width = halfWidth(boat) - (tier == 0 ? 0.2475 : 0.0875);
            double half = length - (tier == 0 ? 0.22 : 0.10);
            double low = tier == 0 ? 0 : 0.16;
            double high = tier == 0 ? 0.16 : HEIGHT + stack;
            int columns = Math.max(1, (int)Math.min(16, Math.ceil(width / 0.65)));
            int slices = Math.max(1, (int)Math.min(512 / columns, Math.ceil(2*half/0.5)));
            double dx=width/columns, dz=half/slices;
            for(int i=0;i<slices;i++) for(int j=0;j<columns;j++) {
                double z=-half+dz*(2*i+1), x=-width+dx*(2*j+1);
                parts.add(BoatBody.box(boat,new Box(x-dx,low,z-dz,x+dx,high,z+dz),yaw));
            }
        }
        List<Box> result = List.copyOf(parts);
        PARTS.get().put(boat, new CachedParts(boat.getPos(), yaw, length, halfWidth(boat), stack, BoatBody.pitch(boat), BoatBody.roll(boat), result));
        return result;
    }

    public static boolean spaceEmpty(BoatEntity boat, float yaw) {
        for (Box part : collisionParts(boat, yaw)) {
            if (!BoatCollisionScene.of(boat).clear(part)) return false;
        }
        return true;
    }

    public static Vec3d clipMovement(BoatEntity boat, Vec3d movement) {
        if (movement.lengthSquared() < 1e-18) return Vec3d.ZERO;
        List<Box> parts = collisionParts(boat, boat.getYaw());
        var scene = BoatCollisionScene.of(boat);
        Vec3d completed = Vec3d.ZERO;
        // Use the vanilla Y-first axis order, carrying the accepted offset into the following axes.
        int[] axes = Math.abs(movement.x) < Math.abs(movement.z) ? new int[] {1, 2, 0} : new int[] {1, 0, 2};
        for (int axis : axes) {
            double desired = axis == 0 ? movement.x : axis == 1 ? movement.y : movement.z;
            double allowed = desired;
            if (Math.abs(desired) < 1e-9) continue;
            for (Box part : parts) {
                Vec3d request = axis == 0 ? new Vec3d(allowed, 0, 0)
                        : axis == 1 ? new Vec3d(0, allowed, 0) : new Vec3d(0, 0, allowed);
                Vec3d clipped = scene.clip(part.offset(completed), request);
                allowed = axis == 0 ? clipped.x : axis == 1 ? clipped.y : clipped.z;
                if (Math.abs(allowed) < 1e-9) break;
            }
            completed = completed.add(axis == 0 ? allowed : 0, axis == 1 ? allowed : 0, axis == 2 ? allowed : 0);
        }
        return completed;
    }

    public static double stackHeight(BoatEntity boat) {
        BoatAccess access = (BoatAccess) boat;
        float spring = access.longboat$visualState().getFloat("Spring");
        return (1 - extension(boat)) * (Math.min(0.35, (access.longboat$segments() - 1) * 0.035)
                + (access.longboat$segments() - 1) * spring * 0.48);
    }

    public static float extension(BoatEntity boat) {
        BoatAccess access = (BoatAccess) boat;
        float t = Math.max(0, Math.min(1, 1 - access.longboat$visualState().getInt("MorphTicks") / (float) BoatAbilities.MORPH_DURATION));
        t = t * t * (3 - 2 * t);
        return access.longboat$compressed() ? 1 - t : t;
    }

    public static Vec3d stern(BoatEntity boat) {
        return boat.getPos().add(rotateY(new Vec3d(0, 0.42, -halfLength(boat) - 0.24), Math.toRadians(boat.getYaw())));
    }

    public static BoatRig previewRig(BoatAccess access) {
        java.util.List<net.minecraft.item.ItemStack> left = OarRack.plain(access.longboat$oars(true));
        java.util.List<net.minecraft.item.ItemStack> right = OarRack.plain(access.longboat$oars(false));
        if (!access.longboat$giants().isEmpty()) {
            left = new java.util.ArrayList<>(); right = new java.util.ArrayList<>();
            for (var mount : access.longboat$giants()) {
                var stack = new net.minecraft.item.ItemStack(net.minecraft.item.Items.WOODEN_SHOVEL);
                GiantOars.setData(stack, mount.units(), mount.slot());
                GiantOars.setScale(stack, mount.scale());
                (mount.left() ? left : right).add(stack);
            }
        }
        return new BoatRig(access.longboat$segments(), left, right, access.longboat$compressed(), access.longboat$puffers(), access.longboat$bottomPuffers(), access.longboat$gridPuffers(), access.longboat$width());
    }

    public static OarPose oarPose(BoatEntity boat, boolean left, int index, float tickDelta) {
        int count = visibleOars(boat, left);
        double usable = Math.max(0.2, halfLength(boat) - 0.35);
        double z = count <= 1 ? 0 : -usable + 2 * usable * index / (count - 1);
        float phase = boat.isPaddleMoving(left ? 0 : 1)
                ? boat.interpolatePaddlePhase(left ? 0 : 1, tickDelta) : 0;
        float sweep = boat.isPaddleMoving(left ? 0 : 1) ? (float) Math.sin(phase) * 0.38f : 0;
        float dip = -0.32f + (boat.isPaddleMoving(left ? 0 : 1) ? (float) Math.cos(phase) * 0.18f : 0);
        int sign = left ? 1 : -1;
        // The shaft rests on top of the gunwale; its inboard grip crosses the rail.
        return new OarPose(new Vec3d(sign * (halfWidth(boat) - 0.06), HEIGHT + 0.06 + stackHeight(boat), z), sweep, dip, sign);
    }

    public static Vec3d rotateY(Vec3d v, double radians) {
        double c = Math.cos(radians), s = Math.sin(radians);
        return new Vec3d(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    public static Vec3d rotateZ(Vec3d v, double radians) {
        double c = Math.cos(radians), s = Math.sin(radians);
        return new Vec3d(v.x * c - v.y * s, v.x * s + v.y * c, v.z);
    }

    private static Vec3d intoOar(Vec3d point, OarPose pose) {
        Vec3d v = point.subtract(pose.pivot());
        v = new Vec3d(v.x * pose.sign(), v.y, v.z);
        return rotateZ(rotateY(v, -pose.yaw()), -pose.dip());
    }

    /** Board/attack only the hull, independently of either hand's modification tool. */
    public static Target raycastHull(PlayerEntity player, float tickDelta) {
        double reach = Math.min(8, player.getEntityInteractionRange());
        Vec3d start = player.getCameraPosVec(tickDelta);
        Vec3d end = start.add(player.getRotationVec(tickDelta).multiply(reach));
        double nearest = unobstructedDistance(player, start, end);
        Target result = null;
        // Vanilla section queries index an entity at its center and can miss a long hull's ends.
        for (BoatEntity boat : LoadedBoats.near(player.getWorld(), new Box(start, end).expand(2))) {
            if (!boat.isAlive() || !boat.canHit()) continue;
            Vec3d localStart = interactionLocal(boat, start, tickDelta);
            Vec3d localEnd = interactionLocal(boat, end, tickDelta);
            double half = boat.getWorld().isClient
                    ? 1 + (((BoatAccess)boat).longboat$segments()-1)*(double)BoatBody.visualExtension(boat,tickDelta)
                    : halfLength(boat);
            double height = HEIGHT + (boat.getWorld().isClient ? BoatBody.visualStackHeight(boat,tickDelta) : stackHeight(boat));
            // The open shell lets a seated player aim at another compartment's floor, even
            // when the eye is inside a tall compressed hull's discovery envelope.
            var hit = mountingHullHit(localStart, localEnd, halfWidth(boat), half, height);
            if (hit.isEmpty()) continue;
            double distance = localStart.squaredDistanceTo(hit.get());
            if (distance < nearest) {
                nearest = distance;
                result = new Target(boat, hit.get().x >= 0, -2, distance, hit.get());
            }
        }
        return result;
    }

    public static Vec3d hitPosition(Target hit, float tickDelta) {
        BoatEntity boat = hit.boat();
        return boat.getWorld().isClient ? BoatBody.visualPosition(boat,tickDelta).add(
                BoatBody.visualOffset(boat,hit.localHit(),BoatBody.visualYaw(boat,tickDelta),tickDelta))
                : BoatBody.world(boat,hit.localHit());
    }

    private static Vec3d interactionLocal(BoatEntity boat, Vec3d point, float tickDelta) {
        if (!boat.getWorld().isClient) return BoatBody.local(boat,point);
        double center = BoatBody.visualCenterY(boat,tickDelta);
        Vec3d v = point.subtract(BoatBody.visualPosition(boat,tickDelta)).add(0,-center,0);
        v = rotateY(v,-Math.toRadians(BoatBody.visualYaw(boat,tickDelta)));
        v = rotateZ(v,-BoatBody.visualRoll(boat,tickDelta));
        return BoatBody.rotateX(v,BoatBody.visualPitch(boat,tickDelta)).add(0,center,0);
    }

    private static double unobstructedDistance(PlayerEntity player, Vec3d start, Vec3d end) {
        HitResult block = player.getWorld().raycast(new RaycastContext(start,end,
                RaycastContext.ShapeType.OUTLINE,RaycastContext.FluidHandling.NONE,player));
        double distance = start.squaredDistanceTo(block.getType()==HitResult.Type.MISS ? end : block.getPos());
        for (Entity entity : player.getWorld().getOtherEntities(player,new Box(start,end).expand(1),
                e -> !(e instanceof BoatEntity) && !e.isSpectator() && e.canHit()
                        && e.getRootVehicle()!=player.getRootVehicle())) {
            Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
            if (box.contains(start)) return 0;
            var hit = box.raycast(start,end);
            if (hit.isPresent()) distance = Math.min(distance,start.squaredDistanceTo(hit.get()));
        }
        return distance;
    }

    /** Validate the clicked hull-local point without requiring two network frames to share a ray. */
    public static boolean canUseHull(PlayerEntity player, BoatEntity boat, Vec3d local) {
        if (!Double.isFinite(local.x) || !Double.isFinite(local.y) || !Double.isFinite(local.z)
                || !boat.isAlive() || boat.getWorld()!=player.getWorld()
                || !boat.getWorld().getWorldBorder().contains(boat.getBlockPos())) return false;
        double width=halfWidth(boat), half=halfLength(boat), height=HEIGHT+stackHeight(boat);
        if (Math.abs(local.x)>width+.05 || Math.abs(local.z)>half+.05 || local.y<-.05 || local.y>height+.05) return false;
        if (local.y>.17 && Math.abs(local.x)<width-.20 && Math.abs(local.z)<half-.20) return false;
        Vec3d start=player.getCameraPosVec(1), end=BoatBody.world(boat,local);
        double reach=Math.min(8,player.getEntityInteractionRange())+1; // Vanilla server interaction tolerance.
        double distance=start.squaredDistanceTo(end);
        if (distance>reach*reach || unobstructedDistance(player,start,end)+0.0025<distance) return false;
        for (BoatEntity other : LoadedBoats.near(player.getWorld(),new Box(start,end).expand(1))) {
            if (other==boat || other==player.getVehicle() || !other.isAlive()) continue;
            Vec3d a=BoatBody.local(other,start), b=BoatBody.local(other,end);
            var hit=mountingHullHit(a,b,halfWidth(other),halfLength(other),HEIGHT+stackHeight(other));
            if (hit.isPresent() && a.squaredDistanceTo(hit.get())+.0025<distance) return false;
        }
        return true;
    }

    public static Target raycast(PlayerEntity player, float tickDelta) {
        return raycast(player, tickDelta, player.getMainHandStack().isOf(net.minecraft.item.Items.MACE)
                || player.getOffHandStack().isOf(net.minecraft.item.Items.MACE));
    }
    public static Target raycast(PlayerEntity player, float tickDelta, boolean pickPuffers) {
        boolean bucket = player.getMainHandStack().isOf(net.minecraft.item.Items.PUFFERFISH_BUCKET)
                || player.getOffHandStack().isOf(net.minecraft.item.Items.PUFFERFISH_BUCKET);
        return raycast(player,tickDelta,pickPuffers,bucket);
    }
    public static Target raycast(PlayerEntity player, float tickDelta, boolean pickPuffers, boolean bucket) {
        double reach = Math.min(8, player.getEntityInteractionRange());
        Vec3d start = player.getCameraPosVec(tickDelta);
        Vec3d end = start.add(player.getRotationVec(tickDelta).multiply(reach));
        HitResult block = player.getWorld().raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        double nearest = block.getType() == HitResult.Type.MISS ? reach * reach : start.squaredDistanceTo(block.getPos());
        for (var entity : player.getWorld().getOtherEntities(player, new Box(start, end).expand(1),
                e -> !(e instanceof BoatEntity) && !e.isSpectator() && e.canHit()
                        && e.getRootVehicle() != player.getRootVehicle())) {
            var hit = entity.getBoundingBox().expand(entity.getTargetingMargin()).raycast(start, end);
            if (hit.isPresent()) nearest = Math.min(nearest, start.squaredDistanceTo(hit.get()));
        }
        Target result = null;
        Box search = new Box(start, end).expand(2);
        for (BoatEntity boat : LoadedBoats.near(player.getWorld(), search.expand(1.5))) {
            if (!boat.isAlive() || !GiantOars.discovery(boat).expand(1.5).intersects(search)) continue;
            double yaw = -Math.toRadians(boat.getYaw());
            Vec3d localStart = BoatBody.local(boat, start);
            Vec3d localEnd = BoatBody.local(boat, end);
            double halfLength = halfLength(boat);
            Box hull = new Box(-halfWidth(boat), 0, -halfLength, halfWidth(boat), HEIGHT + stackHeight(boat), halfLength);
            // Buckets target the actual open hull. A solid discovery box has an invisible lid:
            // it used to intercept downward rays before they reached a gunwale or a long-boat seam.
            var hullHit = bucket && !pickPuffers ? mountingHullHit(localStart,localEnd,
                    halfWidth(boat),halfLength,HEIGHT+stackHeight(boat)) : hull.raycast(localStart, localEnd);
            double hullDistance = hullHit.map(localStart::squaredDistanceTo).orElse(Double.POSITIVE_INFINITY);
            if (hullHit.isPresent() && hullDistance < nearest) {
                Vec3d point = hullHit.get();
                // A bow, stern, or floor hit consumes no material and must not select the far side.
                int sideMarker = point.y <= 1e-5 && localStart.y < -1e-5 ? -4 : point.z <= -halfLength + 0.04 ? -3
                        : Math.abs(point.x) >= halfWidth(boat) - 0.08 ? -1 : -2;
                result = new Target(boat, point.x >= 0, sideMarker, hullDistance, point);
                nearest = hullDistance;
            }
            // Buckets and maces both pick fish; the hit face chooses an outward branch or an upper row.
            if (pickPuffers || bucket) {
                for (var mount : PufferAttachments.near(boat, localStart, reach + 0.5, extension(boat))) {
                    Vec3d center = PufferAttachments.position(boat, mount, extension(boat));
                    var fishHit = new Box(center.add(-0.16, -0.16, -0.16), center.add(0.16, 0.16, 0.16)).raycast(localStart, localEnd);
                    if (fishHit.isPresent() && localStart.squaredDistanceTo(fishHit.get()) < nearest) {
                        nearest = localStart.squaredDistanceTo(fishHit.get());
                        result = new Target(boat, false, -5, nearest, fishHit.get(), mount);
                    }
                }
            }
            if (bucket && !pickPuffers) continue;
            if (!GiantOars.mounted(boat).isEmpty()) {
                for (var mount : GiantOars.mounted(boat)) for (Box box : GiantOars.boxes(boat, mount, GiantOars.phase(boat, mount.left()), boat.getYaw())) {
                    var hit = box.raycast(start, end);
                    if (hit.isPresent() && start.squaredDistanceTo(hit.get()) < nearest) {
                        nearest = start.squaredDistanceTo(hit.get());
                        result = new Target(boat, mount.left(), mount.index(), nearest, BoatBody.local(boat, hit.get()));
                    }
                }
                continue;
            }
            for (boolean left : new boolean[] {true, false}) {
                int count = visibleOars(boat, left);
                for (int i = 0; i < count; i++) {
                    OarPose pose = oarPose(boat, left, i, tickDelta);
                    Vec3d a = intoOar(localStart, pose), b = intoOar(localEnd, pose);
                    for (Box shape : new Box[] {SHAFT, BLADE}) {
                        var hit = shape.expand(0.07).raycast(a, b);
                        if (hit.isEmpty()) continue;
                        double distance = a.squaredDistanceTo(hit.get());
                        if (distance < nearest && distance <= hullDistance + 0.02) {
                            nearest = distance;
                            result = new Target(boat, left, i, distance, pose.pivot());
                        }
                    }
                }
            }
        }
        return result;
    }
    private static java.util.Optional<Vec3d> mountingHullHit(Vec3d start,Vec3d end,double width,double half,double height) {
        Box[] shell={new Box(-width,0,-half,width,0.12,half),
                new Box(-width,0,-half,-width+0.15,height,half),
                new Box(width-0.15,0,-half,width,height,half),
                new Box(-width,0,-half,width,height,-half+0.15),
                new Box(-width,0,half-0.15,width,height,half)};
        Vec3d closest=null;double distance=Double.POSITIVE_INFINITY;
        for(Box part:shell) {
            var hit=part.raycast(start,end);
            if(hit.isPresent() && start.squaredDistanceTo(hit.get())<distance) {
                closest=hit.get();distance=start.squaredDistanceTo(closest);
            }
        }
        return java.util.Optional.ofNullable(closest);
    }
}
