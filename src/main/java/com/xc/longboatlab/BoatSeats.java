package com.xc.longboatlab;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Seat choices belong to passenger UUIDs, not their changing index in the passenger list. */
public final class BoatSeats {
    private BoatSeats() {}
    private static final java.util.Map<Entity,Vec3d> LOCAL_SEATS=java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    public static Vec3d localSeat(BoatEntity boat,Entity passenger) {
        return localSeat(boat,passenger,BoatGeometry.extension(boat),BoatGeometry.stackHeight(boat));
    }
    private static Vec3d localSeat(BoatEntity boat,Entity passenger,float extension,double stack) {
        var a=(BoatAccess)boat; var data=a.longboat$seatData();
        Vec3d old=LOCAL_SEATS.get(passenger);
        double y=old==null ? boat.getHeight()*(boat.getVariant()==BoatEntity.Type.BAMBOO?8.0/9:1.0/3) : old.y;
        if(data.contains(passenger.getUuidAsString())) {
            var entry=data.getCompound(passenger.getUuidAsString());
            return new Vec3d(MathHelper.lerp(extension,entry.getDouble("CompactX"),entry.getDouble("X")),y+stack,MathHelper.lerp(extension,entry.getDouble("CompactZ"),entry.getDouble("Z")));
        }
        int index=Math.max(0,boat.getPassengerList().indexOf(passenger));
        int lane=index%a.longboat$width();
        double z=seatZ(index/a.longboat$width(),BoatGeometry.visibleSegments(boat));
        return new Vec3d(BoatGeometry.laneX(lane,a.longboat$width()),y+stack,z);
    }
    public static Vec3d localFeet(BoatEntity boat,Entity passenger) {
        return localSeat(boat,passenger).subtract(BoatBody.unrotate(boat,passenger.getVehicleAttachmentPos(boat)));
    }
    public static Vec3d visualFeet(BoatEntity boat,Entity passenger,float delta) {
        Vec3d seat=localSeat(boat,passenger,BoatBody.visualExtension(boat,delta),BoatBody.visualStackHeight(boat,delta));
        Vec3d offset=BoatBody.unrotate(boat,passenger.getVehicleAttachmentPos(boat));
        return BoatBody.visualPosition(boat,delta).add(BoatBody.visualOffset(boat,seat.subtract(offset),BoatBody.visualYaw(boat,delta),delta));
    }
    public static java.util.List<net.minecraft.util.math.Box> collisionParts(BoatEntity boat,float yaw) {
        var parts=new java.util.ArrayList<net.minecraft.util.math.Box>();
        for(var rider:boat.getPassengerList()) {
            Vec3d seat=localFeet(boat,rider); var size=rider.getDimensions(rider.getPose());
            double half=size.width()/2.0;
            // A seated player's rendered legs are bent upward. Its entity origin remains 0.6 below the seat,
            // so including the standing leg interval would create an invisible obstacle under the boat.
            double low=rider instanceof PlayerEntity?Math.max(seat.y,localSeat(boat,rider).y-0.12):seat.y;
            double height=Math.max(0.1,seat.y+size.height()-low);
            for(int i=0;i<2;i++)parts.add(BoatBody.box(boat,new net.minecraft.util.math.Box(
                    seat.x-half,low+height*i/2,seat.z-half,
                    seat.x+half,low+height*(i+1)/2,seat.z+half),yaw));
        }
        return parts;
    }


    public static void board(BoatEntity boat, PlayerEntity player, Hand hand, Vec3d hit) {
        BoatAccess access = (BoatAccess) boat;
        if (access.longboat$visualState().getInt("MorphTicks") > 0) {
            BoatNoticePayload.send(player,net.minecraft.text.Text.translatable("message.longboatlab.transforming"));return;
        }
        boolean changingSeat=player.getVehicle()==boat;
        ensureAssigned(boat);
        NbtCompound previous = access.longboat$seatData();
        NbtCompound seats = previous.copy();
        seats.getKeys().removeIf(key -> key.equals(player.getUuidAsString())
                || boat.getPassengerList().stream().noneMatch(e -> e.getUuidAsString().equals(key)));
        boolean compact=access.longboat$compressed();
        int length=compact?1:access.longboat$segments();
        Vec3d chosen=nearestFree(seats,hit,length,access.longboat$width(),compact);
        if(chosen==null) {
            BoatNoticePayload.send(player,net.minecraft.text.Text.translatable("message.longboatlab.seat_full"));return;
        }
        NbtCompound entry=new NbtCompound();
        entry.putDouble("X",chosen.x); entry.putDouble("CompactX",chosen.x);
        entry.putDouble("Z",compact?chosen.z+(access.longboat$segments()%2==0?1:0):chosen.z);
        entry.putDouble("CompactZ",chosen.z>=0?0.4:-0.4);
        if(compact)entry.putDouble("CompactZ",chosen.z);
        seats.put(player.getUuidAsString(),entry);
        access.longboat$setSeatData(seats);
        // Changing seats is not a second mount attempt. Preserve passenger order/controller
        // and avoid the vanilla dismount cooldown or a chest boat's container interaction.
        if (changingSeat) { boat.updatePassengerPosition(player);return; }
        boat.interact(player,hand);
        if(player.getVehicle()!=boat) {
            access.longboat$setSeatData(previous);
            BoatNoticePayload.send(player,net.minecraft.text.Text.translatable("message.longboatlab.board_unavailable"));
        }
    }
    private static double seatZ(long row,int length) {
        return -length+1+(row/2)*2+(row%2==0?-0.4:0.4);
    }
    public static void ensureAssigned(BoatEntity boat) {
        var a=(BoatAccess)boat; var saved=a.longboat$seatData();
        if(saved.getKeys().size()==boat.getPassengerList().size()
                && boat.getPassengerList().stream().allMatch(e->saved.contains(e.getUuidAsString())))return;
        var seats=saved.copy(); boolean changed=false;
        var keys=new java.util.HashSet<>(seats.getKeys());
        for(String key:keys)if(boat.getPassengerList().stream().noneMatch(e->e.getUuidAsString().equals(key))) {
            seats.remove(key);changed=true;
        }
        for(Entity rider:boat.getPassengerList())if(!seats.contains(rider.getUuidAsString())) {
            boolean compact=a.longboat$compressed(); int length=compact?1:a.longboat$segments();
            Vec3d at=nearestFree(seats,new Vec3d(0,0,0.4),length,a.longboat$width(),compact);
            if(at==null)continue;
            var e=new NbtCompound();e.putDouble("X",at.x);e.putDouble("CompactX",at.x);
            e.putDouble("Z",compact?at.z+(a.longboat$segments()%2==0?1:0):at.z);
            e.putDouble("CompactZ",compact?at.z:Math.copySign(0.4,at.z));
            seats.put(rider.getUuidAsString(),e);changed=true;
        }
        if(changed)a.longboat$setSeatData(seats);
    }
    public static int capacity(BoatEntity boat) {
        var a=(BoatAccess)boat;
        int perLane=boat instanceof net.minecraft.entity.vehicle.ChestBoatEntity?1:2;
        return (int)Math.min(Integer.MAX_VALUE,perLane*(double)a.longboat$width()*BoatGeometry.visibleSegments(boat));
    }
    private static Vec3d nearestFree(NbtCompound seats,Vec3d hit,int length,int width,boolean compact) {
        int lane=MathHelper.clamp((int)Math.floor((hit.x+BoatGeometry.HALF_WIDTH*width)/(BoatGeometry.HALF_WIDTH*2)),0,width-1);
        long row=Math.max(0,Math.min(2L*length-1,(long)Math.floor(hit.z+length)));
        int radius=seats.getKeys().size()+1;
        Vec3d best=null; double score=Double.POSITIVE_INFINITY;
        // Search around the chosen compartment, bounded by occupants rather than debug hull size.
        for(long col=Math.max(0,(long)lane-radius);col<=Math.min(width-1L,(long)lane+radius);col++)
            for(long j=Math.max(0,row-radius);j<=Math.min(2L*length-1,row+radius);j++) {
                Vec3d at=new Vec3d(BoatGeometry.laneX((int)col,width),0,seatZ(j,length));
                double d=(at.x-hit.x)*(at.x-hit.x)+(at.z-hit.z)*(at.z-hit.z);
                if(d<score && !occupied(seats,at,compact)){best=at;score=d;}
            }
        return best;
    }
    private static boolean occupied(NbtCompound seats,Vec3d at,boolean compact) {
        for(String key:seats.getKeys()) {
            var e=seats.getCompound(key);
            if(Math.abs(e.getDouble(compact?"CompactX":"X")-at.x)<0.7
                    && Math.abs(e.getDouble(compact?"CompactZ":"Z")-at.z)<0.7)return true;
        }
        return false;
    }
    /** Assign compact seats without overlap; caller restores the snapshot if terrain blocks folding. */
    public static boolean prepareFold(BoatEntity boat) {
        var access=(BoatAccess)boat;
        if(boat.getPassengerList().size()>access.longboat$width()*2L)return false;
        NbtCompound seats=access.longboat$seatData().copy(),assigned=new NbtCompound();
        for(Entity rider:boat.getPassengerList()) {
            Vec3d local=localSeat(boat,rider);
            Vec3d compact=nearestFree(assigned,new Vec3d(local.x,0,Math.copySign(0.4,local.z)),1,access.longboat$width(),true);
            if(compact==null)return false;
            var e=seats.getCompound(rider.getUuidAsString()).copy();
            e.putDouble("X",local.x);e.putDouble("Z",local.z);
            e.putDouble("CompactX",compact.x);e.putDouble("CompactZ",compact.z);
            assigned.put(rider.getUuidAsString(),e);
        }
        access.longboat$setSeatData(assigned);return true;
    }

    public static Vec3d attachment(BoatEntity boat, Entity passenger, Vec3d vanilla) {
        Vec3d base=BoatGeometry.rotateY(vanilla,-Math.toRadians(boat.getYaw()));
        LOCAL_SEATS.put(passenger,base);
        return BoatBody.offset(boat,localSeat(boat,passenger),boat.getYaw());
    }
    public static Vec3d dismount(BoatEntity boat, net.minecraft.entity.LivingEntity passenger, Vec3d fallback) {
        var parts = BoatGeometry.structureParts(boat, boat.getYaw());
        var candidates = new java.util.ArrayList<Vec3d>();
        Vec3d seat = BoatBody.local(boat, passenger.getPos());
        double width = passenger.getWidth()/2 + 0.20;
        for (double side : new double[] {1, -1}) {
            Vec3d edge = BoatBody.world(boat, new Vec3d(side*(BoatGeometry.halfWidth(boat)+width), BoatGeometry.HEIGHT+0.12, seat.z));
            candidates.add(edge);
        }
        Vec3d position = passenger.getPos();
        for (double radius : new double[] {1.25, 2, 3, 5}) for (int direction = 0; direction < 8; direction++) {
            double angle = Math.toRadians(passenger.getYaw()) + direction*Math.PI/4;
            candidates.add(position.add(Math.cos(angle)*radius, 0.15, Math.sin(angle)*radius));
        }
        candidates.add(fallback);
        double top = parts.stream().mapToDouble(b -> b.maxY).max().orElse(boat.getY()+BoatGeometry.HEIGHT);
        candidates.add(new Vec3d(position.x, Math.max(position.y, top+0.12), position.z));
        Vec3d airborne = null;
        for (Vec3d candidate : candidates) {
            var world = boat.getWorld();
            if (!com.xc.longboatlab.BoatEnvironment.isLoaded(world, net.minecraft.util.math.BlockPos.ofFloored(candidate))) continue;
            var floor = world.raycast(new net.minecraft.world.RaycastContext(candidate.add(0, 1, 0), candidate.add(0, -3, 0),
                    net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.ANY, passenger));
            if (floor.getType() != net.minecraft.util.hit.HitResult.Type.MISS) {
                Vec3d feet = new Vec3d(candidate.x, floor.getPos().y+0.10, candidate.z);
                for (var pose : new net.minecraft.entity.EntityPose[] {net.minecraft.entity.EntityPose.STANDING, net.minecraft.entity.EntityPose.CROUCHING}) {
                    if (exitClear(boat, passenger, feet, pose, parts)) { passenger.setPose(pose); return feet; }
                }
            }
            if (airborne == null && exitClear(boat, passenger, candidate, net.minecraft.entity.EntityPose.STANDING, parts)) airborne = candidate;
        }
        if (airborne != null) { passenger.setPose(net.minecraft.entity.EntityPose.STANDING); return airborne; }
        return fallback;
    }
    private static boolean exitClear(BoatEntity boat, net.minecraft.entity.LivingEntity passenger, Vec3d feet,
                                     net.minecraft.entity.EntityPose pose, java.util.List<net.minecraft.util.math.Box> parts) {
        var box = passenger.getDimensions(pose).getBoxAt(feet).expand(0.04, 0.015, 0.04);
        // Vanilla excludes the vehicle while its passenger is dismounting. Explicitly include every hull/oar part.
        for (var part : parts) if (part.expand(0.06).intersects(box)) return false;
        return boat.getWorld().isSpaceEmpty(passenger, box);
    }

}
