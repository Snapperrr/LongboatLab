package com.xc.longboatlab.mixin;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.BoatRig;
import com.xc.longboatlab.OarRack;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {
    protected BoatItemMixin(Settings settings) { super(settings); }

    @Inject(method = "createEntity", at = @At("RETURN"))
    private void longboat$place(World world, HitResult hit, ItemStack stack, PlayerEntity player,
                               CallbackInfoReturnable<BoatEntity> cir) {
        ((BoatAccess) cir.getReturnValue()).longboat$setRig(BoatRig.fromStack(stack, world.getRegistryManager()));
    }

    @Redirect(method = "use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;isSpaceEmpty(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Z"))
    private boolean longboat$placementSpace(World world, Entity entity, Box box) {
        return entity instanceof BoatEntity boat ? BoatGeometry.spaceEmpty(boat, boat.getYaw()) : world.isSpaceEmpty(entity, box);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        NbtCompound custom = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        NbtCompound rig = custom.getCompound(BoatRig.KEY);
        boolean stored = custom.contains(BoatRig.KEY, NbtElement.COMPOUND_TYPE);
        int segments = stored ? Math.max(1, rig.getInt("Segments")) : 1;
        int left = stored ? OarRack.storedCount(rig, "Left") : 1;
        int right = stored ? OarRack.storedCount(rig, "Right") : 1;
        tooltip.add(Text.translatable("tooltip.longboatlab.rig", segments, left, right).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.longboatlab.width", Math.max(1,rig.getInt("Width"))).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.longboatlab.upgrades",
                Text.translatable(!rig.getBoolean("HasGiantOars") && (segments > 1 || rig.getBoolean("Compressed"))
                        ? "hud.longboatlab.compressed" : "hud.longboatlab.extended"),
                rig.getInt("Puffers")).formatted(Formatting.AQUA));
        if (rig.getBoolean("HasGiantOars")) tooltip.add(Text.translatable("tooltip.longboatlab.giant_boat").formatted(Formatting.GOLD));
        int bottom = com.xc.longboatlab.PufferAttachments.count(com.xc.longboatlab.PufferAttachments.decode(rig.getCompound("BottomPuffers")));
        var grid=com.xc.longboatlab.PufferGrid.decode(rig.getCompound("PufferGrid"));
        if (bottom > 0) tooltip.add(Text.translatable("tooltip.longboatlab.puffer_mounts", Math.max(0, rig.getInt("Puffers") - bottom-com.xc.longboatlab.PufferGrid.count(grid)), bottom).formatted(Formatting.GRAY));
        if(!grid.isEmpty()) {
            long[] counts=new long[5];grid.forEach((c,n)->{if(c.face()>=0&&c.face()<5)counts[c.face()]+=n;});
            tooltip.add(Text.translatable("tooltip.longboatlab.grid_puffers",counts[0],counts[1],counts[2],counts[3],counts[4]).formatted(Formatting.AQUA));
        }
        if (left == 0 && right == 0) tooltip.add(Text.translatable("tooltip.longboatlab.no_oars").formatted(Formatting.RED));
    }
}
