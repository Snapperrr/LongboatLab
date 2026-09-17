package com.xc.longboatlab.mixin;

import com.xc.longboatlab.BoatGeometry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityBoundsMixin {
    @Inject(method = "setBoundingBox", at = @At("TAIL"))
    private void longboat$index(Box box, CallbackInfo ci) {
        if ((Object) this instanceof BoatEntity boat) com.xc.longboatlab.LoadedBoats.update(boat);
    }
    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"), cancellable = true)
    private void longboat$hullCollision(net.minecraft.util.math.Vec3d movement,
                                        CallbackInfoReturnable<net.minecraft.util.math.Vec3d> cir) {
        if ((Object) this instanceof BoatEntity boat) cir.setReturnValue(BoatGeometry.clipMovement(boat, movement));
    }

    @Inject(method = "getVisibilityBoundingBox", at = @At("RETURN"), cancellable = true)
    private void longboat$cableVisibility(CallbackInfoReturnable<Box> cir) {
        if ((Object) this instanceof BoatEntity boat) {
            var state = ((com.xc.longboatlab.BoatAccess) boat).longboat$visualState();
            Box box = com.xc.longboatlab.GiantOars.discovery(boat).expand(1.5);
            if (state.getInt("HookState") != 0) {
                double x = state.getDouble("HookX"), y = state.getDouble("HookY"), z = state.getDouble("HookZ");
                box = box.union(new Box(x - 1, y - 4, z - 1, x + 1, y + 1, z + 1));
            }
            cir.setReturnValue(box);
        }
    }

    @Inject(method = "isLogicalSideForUpdatingMovement", at = @At("HEAD"), cancellable = true)
    private void longboat$serverAuthority(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof BoatEntity boat) cir.setReturnValue(!boat.getWorld().isClient);
    }

    @Inject(method = "calculateBoundingBox", at = @At("RETURN"), cancellable = true)
    private void longboat$bounds(CallbackInfoReturnable<Box> cir) {
        if ((Object) this instanceof BoatEntity boat && boat.getDataTracker() != null) {
            cir.setReturnValue(BoatGeometry.bounds(boat));
        }
    }

    @Inject(method = "setYaw", at = @At("TAIL"))
    private void longboat$rotate(float yaw, CallbackInfo ci) {
        if ((Object) this instanceof BoatEntity boat && boat.getDataTracker() != null) {
            boat.setBoundingBox(BoatGeometry.bounds(boat));
        }
    }

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    private void longboat$resize(TrackedData<?> data, CallbackInfo ci) {
        if ((Object) this instanceof BoatEntity boat && boat.getDataTracker() != null) {
            boat.setBoundingBox(BoatGeometry.bounds(boat));
        }
    }
}
