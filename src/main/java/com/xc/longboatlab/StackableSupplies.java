package com.xc.longboatlab;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Compact boat supplies, with independent durability when a shovel is used as a tool. */
public final class StackableSupplies {
    public static final int LIMIT = 16;

    private StackableSupplies() {}

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.PUFFERFISH_BUCKET,
                builder -> builder.add(DataComponentTypes.MAX_STACK_SIZE, LIMIT)));
    }

    public static ItemStack separateTool(ItemStack stack, LivingEntity user, int amount) {
        if (amount <= 0 || user.getWorld().isClient || !stack.isOf(Items.WOODEN_SHOVEL)
                || stack.getCount() <= 1 || !stack.isDamageable()
                || user instanceof PlayerEntity player && player.isCreative()) return ItemStack.EMPTY;
        // Keep this same stack in the hand: vanilla continues damaging it after this call.
        return stack.split(stack.getCount() - 1);
    }

    public static void returnSpare(ItemStack spare, LivingEntity user) {
        if (spare == null || spare.isEmpty()) return;
        if (user instanceof PlayerEntity player) {
            // Called after wear has been applied, so spare shovels cannot merge into the used one.
            player.getInventory().offer(spare, true);
        } else {
            user.dropStack(spare);
        }
    }

    public static boolean insertWornShovels(PlayerInventory inventory, int slot, ItemStack incoming) {
        int before = incoming.getCount();
        if (slot >= 0) {
            mergeSlot(inventory, slot, incoming);
        } else {
            // Vanilla's damaged-tool branch overwrites the chosen slot with one whole stack.
            // Worn shovels can now stack, so merge compatible stacks before using empty slots.
            for (int i = 0; i < inventory.main.size() && !incoming.isEmpty(); i++) {
                if (!inventory.getStack(i).isEmpty()) mergeSlot(inventory, i, incoming);
            }
            if (!incoming.isEmpty() && !inventory.getStack(PlayerInventory.OFF_HAND_SLOT).isEmpty()) {
                mergeSlot(inventory, PlayerInventory.OFF_HAND_SLOT, incoming);
            }
            for (int i = 0; i < inventory.main.size() && !incoming.isEmpty(); i++) {
                if (inventory.getStack(i).isEmpty()) mergeSlot(inventory, i, incoming);
            }
        }
        if (incoming.getCount() != before) inventory.markDirty();
        return incoming.getCount() < before;
    }

    private static void mergeSlot(PlayerInventory inventory, int slot, ItemStack incoming) {
        if (slot < 0 || slot >= inventory.size()) return;
        ItemStack current = inventory.getStack(slot);
        if (current == incoming) return;
        if (current.isEmpty()) {
            inventory.setStack(slot, incoming.split(Math.min(LIMIT, incoming.getCount())));
        } else if (ItemStack.areItemsAndComponentsEqual(current, incoming)) {
            int count = Math.min(incoming.getCount(), Math.max(0, LIMIT - current.getCount()));
            current.increment(count);
            incoming.decrement(count);
        }
    }
}
