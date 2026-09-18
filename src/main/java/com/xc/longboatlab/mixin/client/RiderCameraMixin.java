package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.BoatBodyView;
import com.xc.longboatlab.client.RearCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.*;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class RiderCameraMixin {
    @Shadow private Entity focusedEntity;
    @Shadow private float lastTickDelta;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f horizontalPlane;
    @Shadow @Final private Vector3f verticalPlane;
    @Shadow @Final private Vector3f diagonalPlane;
    @Inject(method="setRotation",at=@At("TAIL"))
    private void longboat$cameraFrame(float yaw,float pitch,CallbackInfo ci) {
        if (RearCamera.active()) return;
        if(focusedEntity!=null && focusedEntity.getVehicle() instanceof BoatEntity boat) {
            var tilt=BoatBodyView.riderRotation(boat,lastTickDelta);
            rotation.premul(tilt);horizontalPlane.rotate(tilt);verticalPlane.rotate(tilt);diagonalPlane.rotate(tilt);
        }
    }
    @ModifyArgs(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/Camera;setRotation(FF)V",ordinal=0))
    private void longboat$rearHeading(Args args) {
        if (!RearCamera.active() || focusedEntity == null || !(focusedEntity.getVehicle() instanceof BoatEntity boat)) return;
        // Capture the rear view once on entry. Even a spinning hull cannot rotate this frame.
        args.set(0, RearCamera.yaw());
        args.set(1, 12.0f);
    }
    @ModifyArg(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/Camera;clipToSpace(F)F"),index=0)
    private float longboat$rearDistance(float vanilla) {
        return RearCamera.active() && focusedEntity != null && focusedEntity.getVehicle() instanceof BoatEntity
                ? RearCamera.distance(lastTickDelta) : vanilla;
    }
    @ModifyArgs(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void longboat$cameraEye(Args args) {
        if (RearCamera.active() && focusedEntity!=null && focusedEntity.getVehicle() instanceof BoatEntity boat) {
            // Orbiting seats on a long rotating hull must not drag the stable camera in circles.
            var center=com.xc.longboatlab.BoatBody.visualPosition(boat,lastTickDelta).add(0,1.35,0);
            args.set(0,center.x);args.set(1,center.y);args.set(2,center.z);
            return;
        }
        if(focusedEntity!=null && focusedEntity.getVehicle() instanceof BoatEntity boat) {
            var e=focusedEntity;double delta=lastTickDelta;
            Vec3d feet=new Vec3d(MathHelper.lerp(delta,e.prevX,e.getX()),MathHelper.lerp(delta,e.prevY,e.getY()),MathHelper.lerp(delta,e.prevZ,e.getZ()));
            Vector3f offset=new Vector3f((float)((double)args.get(0)-feet.x),(float)((double)args.get(1)-feet.y),(float)((double)args.get(2)-feet.z));
            offset.rotate(BoatBodyView.riderRotation(boat,lastTickDelta));
            Vec3d anchor=com.xc.longboatlab.BoatSeats.visualFeet(boat,e,lastTickDelta);
            args.set(0,anchor.x+offset.x);args.set(1,anchor.y+offset.y);args.set(2,anchor.z+offset.z);
        }
    }
}
