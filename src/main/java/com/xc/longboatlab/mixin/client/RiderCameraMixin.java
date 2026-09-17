package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.BoatBodyView;
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
        if(focusedEntity!=null && focusedEntity.getVehicle() instanceof BoatEntity boat) {
            var tilt=BoatBodyView.riderRotation(boat,lastTickDelta);
            rotation.premul(tilt);horizontalPlane.rotate(tilt);verticalPlane.rotate(tilt);diagonalPlane.rotate(tilt);
        }
    }
    @ModifyArgs(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void longboat$cameraEye(Args args) {
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
