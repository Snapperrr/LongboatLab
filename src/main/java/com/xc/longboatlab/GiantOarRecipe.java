package com.xc.longboatlab;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

/** Three or more shovels merge their volume, leaving vanilla's two-item repair recipe unambiguous. */
public final class GiantOarRecipe extends SpecialCraftingRecipe {
    public GiantOarRecipe(CraftingRecipeCategory category) { super(category); }
    private ItemStack result(CraftingRecipeInput input) {
        ItemStack base = ItemStack.EMPTY;
        int ingredients = 0;
        long units = 0;
        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (!stack.isOf(Items.WOODEN_SHOVEL)) return ItemStack.EMPTY;
            if (base.isEmpty()) base = stack;
            units += GiantOars.units(stack);
            ingredients++;
        }
        if (ingredients < 3 || units > Integer.MAX_VALUE) return ItemStack.EMPTY;
        ItemStack output = base.copyWithCount(1);
        GiantOars.setData(output, (int) units, -1);
        GiantOars.setScale(output, GiantOars.scale((int) units));
        output.set(net.minecraft.component.DataComponentTypes.ITEM_NAME, net.minecraft.text.Text.translatable("item.longboatlab.giant_oar"));
        return output;
    }
    @Override public boolean matches(CraftingRecipeInput input, World world) { return !result(input).isEmpty(); }
    @Override public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) { return result(input); }
    @Override public boolean fits(int width, int height) { return width * height >= 3; }
    @Override public RecipeSerializer<?> getSerializer() { return LongboatLab.GIANT_OAR; }
}
