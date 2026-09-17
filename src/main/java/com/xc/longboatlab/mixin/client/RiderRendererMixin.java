package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.BoatBodyView;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class RiderRendererMixin {
    @Inject(method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("HEAD"))
    private void longboat$pose(LivingEntity rider,float yaw,float delta,MatrixStack matrices,VertexConsumerProvider consumers,int light,CallbackInfo ci) {
        matrices.push();
        if(rider.getVehicle() instanceof BoatEntity boat) {
            var interpolated=new net.minecraft.util.math.Vec3d(
                    net.minecraft.util.math.MathHelper.lerp(delta,rider.lastRenderX,rider.getX()),
                    net.minecraft.util.math.MathHelper.lerp(delta,rider.lastRenderY,rider.getY()),
                    net.minecraft.util.math.MathHelper.lerp(delta,rider.lastRenderZ,rider.getZ()));
            var correction=com.xc.longboatlab.BoatSeats.visualFeet(boat,rider,delta).subtract(interpolated);
            matrices.translate(correction.x,correction.y,correction.z);
            matrices.multiply(BoatBodyView.riderRotation(boat,delta));
        }
    }
    @Inject(method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("RETURN"))
    private void longboat$restore(LivingEntity rider,float yaw,float delta,MatrixStack matrices,VertexConsumerProvider consumers,int light,CallbackInfo ci) {matrices.pop();}
}
