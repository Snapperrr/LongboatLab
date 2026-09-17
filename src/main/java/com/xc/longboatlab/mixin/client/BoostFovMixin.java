package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.BoatKeys;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class BoostFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void longboat$boostFov(Camera camera, float tickDelta, boolean changing,
                                  CallbackInfoReturnable<Double> cir) {
        if (changing) cir.setReturnValue(cir.getReturnValue() * (1 + BoatKeys.fovBoost(tickDelta)));
    }
}
