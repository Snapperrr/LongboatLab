package com.xc.longboatlab.mixin;

import com.xc.longboatlab.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Server and client interaction rays share the rider's rolled frame, including the grapple aim. */
@Mixin(Entity.class)
public abstract class RiderFrameMixin {
    @Inject(method="getVehicleAttachmentPos",at=@At("RETURN"),cancellable=true)
    private void longboat$rotatedAttachment(Entity vehicle,CallbackInfoReturnable<Vec3d> cir) {
        if(vehicle instanceof BoatEntity boat) {
            // Entity.updatePassengerPosition subtracts this offset after positioning the seat anchor.
            // Rotate both anchors in the same frame, rather than subtracting a world-upright vector.
            Vec3d local=BoatGeometry.rotateY(cir.getReturnValue(),-Math.toRadians(boat.getYaw()));
            cir.setReturnValue(BoatBody.direction(boat,local));
        }
    }
    @Inject(method="getRotationVec",at=@At("RETURN"),cancellable=true)
    private void longboat$look(float delta,CallbackInfoReturnable<Vec3d> cir) {
        Entity rider=(Entity)(Object)this;
        if(rider.getVehicle() instanceof BoatEntity boat)cir.setReturnValue(BoatBody.riderDirection(boat,cir.getReturnValue(),delta));
    }
    @Inject(method="getCameraPosVec",at=@At("RETURN"),cancellable=true)
    private void longboat$eye(float delta,CallbackInfoReturnable<Vec3d> cir) {
        Entity rider=(Entity)(Object)this;
        if(rider.getVehicle() instanceof BoatEntity boat) {
            Vec3d feet=new Vec3d(MathHelper.lerp(delta,rider.prevX,rider.getX()),MathHelper.lerp(delta,rider.prevY,rider.getY()),MathHelper.lerp(delta,rider.prevZ,rider.getZ()));
            Vec3d anchor=boat.getWorld().isClient?BoatSeats.visualFeet(boat,rider,delta):feet;
            cir.setReturnValue(anchor.add(BoatBody.riderDirection(boat,cir.getReturnValue().subtract(feet),delta)));
        }
    }
}
