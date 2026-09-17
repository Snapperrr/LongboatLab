package com.xc.longboatlab.mixin;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla coordinate packets must not override the server's grapple constraints. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerBoatMovementMixin {
    @Shadow public ServerPlayerEntity player;
    @Inject(method = "onVehicleMove", at = @At("HEAD"), cancellable = true)
    private void longboat$movement(VehicleMoveC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        if (player.getRootVehicle() instanceof BoatEntity) ci.cancel();
    }
    @Inject(method = "onBoatPaddleState", at = @At("HEAD"), cancellable = true)
    private void longboat$paddles(BoatPaddleStateC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        if (player.getVehicle() instanceof BoatEntity) ci.cancel();
    }
}
