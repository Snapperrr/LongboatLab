package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.water.WaterSurfaceShaders;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class WaterTerrainShaderMixin {
    @Inject(method = "getRenderTypeTranslucentProgram", at = @At("RETURN"), cancellable = true)
    private static void longboat$maskReplacedWater(CallbackInfoReturnable<ShaderProgram> cir) {
        cir.setReturnValue(WaterSurfaceShaders.terrainProgram(cir.getReturnValue()));
    }
}
