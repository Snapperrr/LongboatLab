package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.LongboatRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.Frustum;
import com.xc.longboatlab.BoatAccess;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntityRenderer.class)
public abstract class BoatRendererMixin extends EntityRenderer<BoatEntity> {
    protected BoatRendererMixin(EntityRendererFactory.Context context) { super(context); }

    @Override
    public boolean shouldRender(BoatEntity boat, Frustum frustum, double x, double y, double z) {
        if (((BoatAccess) boat).longboat$visualState().getInt("HookState") != 0) {
            return frustum.isVisible(boat.getVisibilityBoundingBox());
        }
        return super.shouldRender(boat, frustum, x, y, z);
    }

    @Inject(method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void longboat$render(BoatEntity boat, float yaw, float tickDelta, MatrixStack matrices,
                                VertexConsumerProvider consumers, int light, CallbackInfo ci) {
        LongboatRenderer.render(boat, yaw, tickDelta, matrices, consumers, light);
        super.render(boat, yaw, tickDelta, matrices, consumers, light);
        ci.cancel();
    }
}
