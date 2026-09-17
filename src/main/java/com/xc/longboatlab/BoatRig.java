package com.xc.longboatlab;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

/** Item stacks are copied at boundaries so crafting previews cannot mutate their ingredients. */
public record BoatRig(int segments, List<ItemStack> left, List<ItemStack> right, boolean compressed, int puffers, java.util.Map<Integer, Integer> bottomPuffers, java.util.Map<PufferGrid.Cell, Integer> gridPuffers, int width) {
    public BoatRig(int segments, List<ItemStack> left, List<ItemStack> right, boolean compressed, int puffers, java.util.Map<Integer,Integer> bottom, java.util.Map<PufferGrid.Cell,Integer> grid) {
        this(segments,left,right,compressed,puffers,bottom,grid,1);
    }
    public BoatRig(int segments, List<ItemStack> left, List<ItemStack> right, boolean compressed, int puffers, java.util.Map<Integer,Integer> bottom) {
        this(segments,left,right,compressed,puffers,bottom,java.util.Map.of());
    }
    public static final String KEY = "LongboatRig";
    public static final int MAX_SEGMENTS = 16;
    public static int oarLimit(int segments) { return (int) Math.min(Integer.MAX_VALUE, segments * 16L); }
    public BoatRig(int segments, List<ItemStack> left, List<ItemStack> right, boolean compressed, int puffers) {
        this(segments, left, right, compressed, puffers, java.util.Map.of());
    }

    public BoatRig(int segments, List<ItemStack> left, List<ItemStack> right) {
        this(segments, left, right, false, 0);
    }

    public BoatRig {
        // Survival limits are enforced by crafting and attachment interactions, not by persistence.
        segments = Math.max(1, segments);
        width = Math.max(1, width);
        puffers = Math.max(0, puffers);
        bottomPuffers = PufferAttachments.sanitize(bottomPuffers, segments, puffers);
        gridPuffers = PufferGrid.sanitize(gridPuffers, segments, width, puffers-PufferAttachments.count(bottomPuffers));
        left = OarRack.of(left);
        right = OarRack.of(right);
        if (OarRack.of(left).hasGiants() || OarRack.of(right).hasGiants()) compressed = false;
    }

    public static BoatRig vanilla() {
        return new BoatRig(1, List.of(new ItemStack(Items.WOODEN_SHOVEL)),
                List.of(new ItemStack(Items.WOODEN_SHOVEL)));
    }

    public BoatRig withWidth(int value) { return new BoatRig(segments,left,right,compressed,puffers,bottomPuffers,gridPuffers,value); }
    public int oarLimit() { return (int)Math.min(Integer.MAX_VALUE, segments * (double)width * 16); }
    public boolean hasGiants() { return OarRack.of(left).hasGiants() || OarRack.of(right).hasGiants(); }

    public List<ItemStack> side(boolean isLeft) { return isLeft ? left : right; }

    public BoatRig add(boolean isLeft, ItemStack shovel) {
        return add(isLeft,shovel,1);
    }
    public BoatRig add(boolean isLeft, ItemStack shovel,int amount) {
        List<ItemStack> changed = OarRack.of(side(isLeft)).append(shovel,amount);
        return new BoatRig(segments, isLeft ? changed : left, isLeft ? right : changed, compressed, puffers, bottomPuffers, gridPuffers, width);
    }

    public BoatRig remove(boolean isLeft, int index) {
        List<ItemStack> changed = OarRack.of(side(isLeft)).without(index);
        return new BoatRig(segments, isLeft ? changed : left, isLeft ? right : changed, compressed, puffers, bottomPuffers, gridPuffers, width);
    }

    public BoatRig withMode(boolean mode) { return new BoatRig(segments, left, right, mode, puffers, bottomPuffers, gridPuffers, width); }
    public BoatRig withPuffers(int count) { return new BoatRig(segments, left, right, compressed, count, bottomPuffers, gridPuffers, width); }

    public int sternPuffers() { return puffers - PufferAttachments.count(bottomPuffers) - PufferGrid.count(gridPuffers); }
    public BoatRig changePuffer(int segment, int delta) {
        if ((delta > 0 && puffers == Integer.MAX_VALUE) || (delta < 0 && puffers == 0)) return this;
        var next = new java.util.TreeMap<>(bottomPuffers);
        if (segment >= 0) {
            int count = next.getOrDefault(segment, 0) + delta;
            if (count < 0) return this;
            if (count == 0) next.remove(segment); else next.put(segment, count);
        } else if (delta < 0 && sternPuffers() == 0) return this;
        return new BoatRig(segments, left, right, compressed, puffers + delta, next, gridPuffers, width);
    }

    public BoatRig changeGrid(PufferGrid.Cell cell, int delta) {
        if ((delta>0 && puffers==Integer.MAX_VALUE) || gridPuffers.getOrDefault(cell,0)+ (long)delta<0) return this;
        var cells=new java.util.LinkedHashMap<>(gridPuffers); int n=cells.getOrDefault(cell,0)+delta;
        if(n==0)cells.remove(cell);else cells.put(cell,n);
        return new BoatRig(segments,left,right,compressed,puffers+delta,bottomPuffers,cells,width);
    }
    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound result = new NbtCompound();
        result.putInt("Segments", segments);
        result.putInt("Width", width);
        result.putBoolean("Compressed", compressed);
        result.putInt("Puffers", puffers);
        result.put("BottomPuffers", PufferAttachments.encode(bottomPuffers));
        result.put("PufferGrid", PufferGrid.encode(gridPuffers));
        result.putBoolean("HasGiantOars", hasGiants());
        OarRack.of(left).write(result, "Left", registries);
        OarRack.of(right).write(result, "Right", registries);
        return result;
    }

    public static BoatRig fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        return new BoatRig(nbt.getInt("Segments"), OarRack.read(nbt, "Left", registries),
                OarRack.read(nbt, "Right", registries),
                nbt.getBoolean("Compressed"), nbt.getInt("Puffers"), PufferAttachments.decode(nbt.getCompound("BottomPuffers")), PufferGrid.decode(nbt.getCompound("PufferGrid")), nbt.getInt("Width"));
    }

    public static BoatRig fromStack(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        NbtCompound custom = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        BoatRig rig = custom.contains(KEY, NbtElement.COMPOUND_TYPE) ? fromNbt(custom.getCompound(KEY), registries) : vanilla();
        return rig.segments() > 1 ? rig.withMode(true) : rig;
    }

    public void applyToStack(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        NbtCompound custom = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        custom.put(KEY, (segments > 1 ? withMode(true) : this).toNbt(registries));
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(custom));
    }

}
