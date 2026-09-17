package com.xc.longboatlab;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BoatItem;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.World;

public final class ExtendBoatRecipe extends SpecialCraftingRecipe {
    public ExtendBoatRecipe(CraftingRecipeCategory category) { super(category); }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return !assemble(input, world.getRegistryManager()).isEmpty();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return assemble(input, registries);
    }

    private ItemStack assemble(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
        ItemStack base = ItemStack.EMPTY;
        List<BoatRig> rigs = new ArrayList<>();
        Boolean giantMode = null;
        for(int i=0;i<input.getSize();i++) {
            ItemStack stack=input.getStackInSlot(i); if(stack.isEmpty())continue;
            if(!(stack.getItem() instanceof BoatItem) || !stack.isIn(ItemTags.BOATS) || stack.isIn(ItemTags.CHEST_BOATS))return ItemStack.EMPTY;
            if(base.isEmpty())base=stack;
            if(!stack.isOf(base.getItem()))return ItemStack.EMPTY;
            BoatRig rig=BoatRig.fromStack(stack,registries);
            if(rig.segments()>BoatRig.MAX_SEGMENTS || rig.width()>BoatRig.MAX_SEGMENTS
                    || rig.left().size()>rig.oarLimit() || rig.right().size()>rig.oarLimit())return ItemStack.EMPTY;
            if(!rig.left().isEmpty() || !rig.right().isEmpty()) {
                if(giantMode!=null && giantMode!=rig.hasGiants())return ItemStack.EMPTY;
                giantMode=rig.hasGiants();
            }
            rigs.add(rig);
        }
        if(rigs.size()<2)return ItemStack.EMPTY;
        BoatRig first=rigs.getFirst();
        boolean widen=first.segments()>=2 && rigs.stream().allMatch(r->r.segments()==first.segments());
        if(!widen && rigs.stream().anyMatch(r->r.width()!=first.width()))return ItemStack.EMPTY;
        int length=widen?first.segments():rigs.stream().mapToInt(BoatRig::segments).sum();
        int width=widen?rigs.stream().mapToInt(BoatRig::width).sum():first.width();
        if(length>BoatRig.MAX_SEGMENTS || width>BoatRig.MAX_SEGMENTS)return ItemStack.EMPTY;
        List<ItemStack> left=new ArrayList<>(),right=new ArrayList<>();
        var usedLeft=new java.util.HashSet<Integer>(); var usedRight=new java.util.HashSet<Integer>();
        var grid=new java.util.LinkedHashMap<PufferGrid.Cell,Integer>();
        long total=0; int offset=0;
        for(BoatRig rig:rigs) {
            total+=rig.puffers(); if(total>Integer.MAX_VALUE)return ItemStack.EMPTY;
            for(boolean port:new boolean[]{true,false})for(ItemStack oar:rig.side(port)) {
                ItemStack copy=oar.copy();
                if(GiantOars.giant(copy)) {
                    int slot=GiantOars.slot(copy)+(widen?0:offset);
                    // Move a conflicting giant to the nearest free gunwale slot without changing its size/data.
                    var used=port?usedLeft:usedRight;
                    if(used.contains(slot)) {
                        int nearest=-1;
                        for(int candidate=0;candidate<length;candidate++)
                            if(!used.contains(candidate) && (nearest<0 || Math.abs(candidate-slot)<Math.abs(nearest-slot)))nearest=candidate;
                        if(nearest<0)return ItemStack.EMPTY;
                        slot=nearest;
                    }
                    used.add(slot);
                    GiantOars.setData(copy,GiantOars.units(copy),slot);
                }
                (port?left:right).add(copy);
            }
            for(var e:rig.gridPuffers().entrySet()) {
                var c=e.getKey();
                int slot=c.face()==PufferGrid.STERN||c.face()==PufferGrid.BOW?0:c.slot()+(widen?0:offset);
                int lane=c.face()>=PufferGrid.LEFT?0:c.lane()+(widen?offset:0);
                merge(grid,c.moved(slot,lane),e.getValue());
            }
            // Migrate legacy mounts to explicit cells so lateral assembly preserves their compartments.
            int lane=(rig.width()-1)/2+(widen?offset:0);
            for(var e:rig.bottomPuffers().entrySet())for(int i=0;i<15;i++) {
                int n=e.getValue()/15+(i<e.getValue()%15?1:0); if(n==0)continue;
                var cell=PufferAttachments.cell(i);
                merge(grid,new PufferGrid.Cell(PufferGrid.BOTTOM,e.getKey()+(widen?0:offset),
                        (int)Math.round(cell.x/0.34),(int)Math.round(cell.z/0.34),lane,0,false),n);
            }
            // Four legacy end columns become four distinct outward branches, conserving every fish.
            int stern=rig.sternPuffers();
            for(int i=0;i<4;i++) {
                int n=stern/4+(i<stern%4?1:0); if(n>0)
                    merge(grid,new PufferGrid.Cell(PufferGrid.STERN,0,i%3-1,i/3,lane,0,true),n);
            }
            offset+=widen?rig.width():rig.segments();
        }
        BoatRig assembled=new BoatRig(length,left,right,true,(int)total,java.util.Map.of(),grid,width);
        if(left.size()>assembled.oarLimit() || right.size()>assembled.oarLimit())return ItemStack.EMPTY;
        ItemStack result=base.copyWithCount(1); assembled.applyToStack(result,registries); return result;
    }
    private static void merge(java.util.Map<PufferGrid.Cell,Integer> grid,PufferGrid.Cell cell,int count) {
        // Total puffer count is checked before each rig, so a cell cannot overflow.
        grid.merge(cell,count,Integer::sum);
    }

    @Override public boolean fits(int width, int height) { return width * height >= 2; }
    @Override public RecipeSerializer<?> getSerializer() { return LongboatLab.EXTEND_BOAT; }
}
