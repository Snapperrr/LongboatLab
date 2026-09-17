package com.xc.longboatlab.mixin;

import com.xc.longboatlab.GiantOars;
import net.minecraft.item.Items;
import net.minecraft.recipe.RepairItemRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Vanilla repair discards custom components; never silently turn giant oars back into ordinary shovels. */
@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z",
            at = @At("HEAD"), cancellable = true)
    private void longboat$preserveSize(CraftingRecipeInput input, World world, CallbackInfoReturnable<Boolean> cir) {
        for (int i = 0; i < input.getSize(); i++) {
            var stack = input.getStackInSlot(i);
            if (stack.isOf(Items.WOODEN_SHOVEL) && GiantOars.giant(stack)) { cir.setReturnValue(false); return; }
        }
    }
}
