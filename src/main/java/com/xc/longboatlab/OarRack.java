package com.xc.longboatlab;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

/** Count ordinary shovels without allocating one object per debug oar; store only customized exceptions. */
public final class OarRack extends AbstractList<ItemStack> {
    private static final ItemStack DEFAULT = new ItemStack(Items.WOODEN_SHOVEL);
    private final int count;
    private final Map<Integer, ItemStack> custom;
    private OarRack(int count, Map<Integer, ItemStack> custom) { this.count = count; this.custom = Map.copyOf(custom); }
    public static OarRack plain(int count) { return new OarRack(Math.max(0, count), Map.of()); }
    public static OarRack of(List<ItemStack> source) {
        if (source instanceof OarRack rack) return rack;
        Map<Integer, ItemStack> custom = new HashMap<>();
        int count = 0;
        for (ItemStack stack : source) {
            if (!stack.isOf(Items.WOODEN_SHOVEL)) continue;
            if (!ItemStack.areItemsAndComponentsEqual(stack, DEFAULT)) custom.put(count, stack.copyWithCount(1));
            count++;
        }
        return new OarRack(count, custom);
    }
    public Map<Integer, ItemStack> specials() { return custom; }
    public boolean hasGiants() { return custom.values().stream().anyMatch(GiantOars::giant); }
    @Override public int size() { return count; }
    @Override public ItemStack get(int index) {
        Objects.checkIndex(index, count);
        return custom.getOrDefault(index, DEFAULT).copyWithCount(1);
    }
    public OarRack append(ItemStack stack) {
        return append(stack,1);
    }
    public OarRack append(ItemStack stack,int amount) {
        amount=(int)Math.min(Math.max(0,amount),Integer.MAX_VALUE-(long)count);
        if(amount==0)return this;
        Map<Integer, ItemStack> next = new HashMap<>(custom);
        if (!ItemStack.areItemsAndComponentsEqual(stack, DEFAULT))
            for(int i=0;i<amount;i++)next.put(count+i,stack.copyWithCount(1));
        return new OarRack(count + amount, next);
    }
    public OarRack without(int index) {
        Objects.checkIndex(index, count);
        Map<Integer, ItemStack> next = new HashMap<>();
        custom.forEach((key, stack) -> { if (key != index) next.put(key > index ? key - 1 : key, stack); });
        return new OarRack(count - 1, next);
    }
    public void write(NbtCompound tag, String side, RegistryWrapper.WrapperLookup registries) {
        tag.putInt(side + "Count", count);
        NbtList list = new NbtList();
        custom.forEach((index, stack) -> {
            NbtCompound entry = new NbtCompound();
            entry.putInt("Index", index);
            entry.put("Stack", stack.encode(registries));
            list.add(entry);
        });
        tag.put(side + "Custom", list);
    }
    public static OarRack read(NbtCompound tag, String side, RegistryWrapper.WrapperLookup registries) {
        if (!tag.contains(side + "Count", NbtElement.NUMBER_TYPE)) {
            NbtList legacy = tag.getList(side, NbtElement.COMPOUND_TYPE);
            java.util.ArrayList<ItemStack> stacks = new java.util.ArrayList<>();
            for (int i = 0; i < legacy.size(); i++) ItemStack.fromNbt(registries, legacy.getCompound(i)).ifPresent(stacks::add);
            return of(stacks);
        }
        int count = Math.max(0, tag.getInt(side + "Count"));
        Map<Integer, ItemStack> custom = new HashMap<>();
        NbtList list = tag.getList(side + "Custom", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            int index = entry.getInt("Index");
            if (index >= 0 && index < count) ItemStack.fromNbt(registries, entry.getCompound("Stack"))
                    .filter(s -> s.isOf(Items.WOODEN_SHOVEL)).ifPresent(s -> custom.put(index, s.copyWithCount(1)));
        }
        return new OarRack(count, custom);
    }
    public static int storedCount(NbtCompound tag, String side) {
        return tag.contains(side + "Count", NbtElement.NUMBER_TYPE) ? Math.max(0, tag.getInt(side + "Count"))
                : tag.getList(side, NbtElement.COMPOUND_TYPE).size();
    }
}
