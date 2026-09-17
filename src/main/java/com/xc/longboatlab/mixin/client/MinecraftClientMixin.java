package com.xc.longboatlab.mixin.client;

import com.xc.longboatlab.client.LongboatClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow private int itemUseCooldown;
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void longboat$interact(CallbackInfo ci) {
        if (LongboatClient.interact((MinecraftClient) (Object) this)) {
            itemUseCooldown=4;
            ci.cancel();
        }
    }
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void longboat$attack(CallbackInfoReturnable<Boolean> cir) {
        if (LongboatClient.attack((MinecraftClient)(Object)this)) cir.setReturnValue(false);
    }
}
