package com.xc.longboatlab.mixin;

import com.xc.longboatlab.StackableSupplies;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class SupplyItemStackMixin {
    @Unique private ItemStack longboat$spareTools;
    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void longboat$supplyCount(CallbackInfoReturnable<Integer> cir) {
        // Retain the vanilla max_damage / max_stack_size=1 component pair. Minecraft 1.21.1
        // rejects serialized damageable components with max_stack_size > 1. Inventory and
        // count validation both use getMaxCount(), which can safely provide our tool limit.
        if (((ItemStack) (Object) this).isOf(Items.WOODEN_SHOVEL)) cir.setReturnValue(StackableSupplies.LIMIT);
    }

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void longboat$matchingShovels(CallbackInfoReturnable<Boolean> cir) {
        // Vanilla inventory still compares every component: different wear, names and oar sizes do not merge.
        if (((ItemStack) (Object) this).isOf(Items.WOODEN_SHOVEL)) cir.setReturnValue(true);
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            at = @At("HEAD"))
    private void longboat$separateDurability(int amount, LivingEntity user, EquipmentSlot slot, CallbackInfo ci) {
        longboat$spareTools = StackableSupplies.separateTool((ItemStack) (Object) this, user, amount);
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V",
            at = @At("RETURN"))
    private void longboat$returnUnused(int amount, LivingEntity user, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack spare = longboat$spareTools;
        longboat$spareTools = null;
        StackableSupplies.returnSpare(spare, user);
    }
}
