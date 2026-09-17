package com.xc.longboatlab.mixin;

import com.xc.longboatlab.BoatAccess;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {
    @Inject(method = "shouldAlwaysKill", at = @At("HEAD"), cancellable = true)
    private void longboat$creativeDrop(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof BoatEntity && source.getAttacker() instanceof PlayerEntity player
                && player.getAbilities().creativeMode) {
            // Use the normal polymorphic destruction path, including chest contents and rig data.
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "killAndDropItem", at = @At("STORE"), ordinal = 0)
    private ItemStack longboat$keepRig(ItemStack stack) {
        if ((Object) this instanceof BoatEntity boat) {
            ((BoatAccess) boat).longboat$rig().applyToStack(stack, boat.getRegistryManager());
        }
        return stack;
    }
}
