package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.RearCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method="onMouseScroll", at=@At("HEAD"), cancellable=true)
    private void longboat$rearCameraWheel(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window==client.getWindow().getHandle() && RearCamera.scroll(client, vertical)) ci.cancel();
    }
}
