package com.xc.longboatlab.mixin;

import com.xc.longboatlab.StackableSupplies;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class SupplyInventoryMixin {
    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void longboat$mergeWornSupplies(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(Items.WOODEN_SHOVEL) && stack.isDamaged()) {
            cir.setReturnValue(StackableSupplies.insertWornShovels((PlayerInventory) (Object) this, slot, stack));
        }
    }
}
