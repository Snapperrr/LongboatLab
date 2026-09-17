package com.xc.longboatlab.mixin;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.BoatRig;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatDispenserBehavior.class)
public abstract class BoatDispenserMixin {
    @Shadow @Final private BoatEntity.Type boatType;
    @Shadow @Final private boolean chest;
    @Shadow @Final private ItemDispenserBehavior itemDispenser;

    @Inject(method = "dispenseSilently", at = @At("HEAD"), cancellable = true)
    private void longboat$dispense(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
                .copyNbt().contains(BoatRig.KEY, NbtElement.COMPOUND_TYPE)) return;
        Direction direction = pointer.state().get(DispenserBlock.FACING);
        BlockPos front = pointer.pos().offset(direction);
        double height;
        if (pointer.world().getFluidState(front).isIn(FluidTags.WATER)) height = 1;
        else if (pointer.world().getBlockState(front).isAir()
                && pointer.world().getFluidState(front.down()).isIn(FluidTags.WATER)) height = 0;
        else {
            cir.setReturnValue(itemDispenser.dispense(pointer, stack));
            return;
        }
        BoatRig rig = BoatRig.fromStack(stack, pointer.world().getRegistryManager());
        double distance = 0.5625 + (rig.compressed() ? 1 : rig.segments());
        Vec3d pos = pointer.centerPos().add(direction.getOffsetX() * distance,
                direction.getOffsetY() * 1.125 + height, direction.getOffsetZ() * distance);
        BoatEntity boat = chest ? new ChestBoatEntity(pointer.world(), pos.x, pos.y, pos.z)
                : new BoatEntity(pointer.world(), pos.x, pos.y, pos.z);
        EntityType.copier(pointer.world(), stack, null).accept(boat);
        boat.setVariant(boatType);
        ((BoatAccess) boat).longboat$setRig(rig);
        boat.setYaw(direction.asRotation());
        if (BoatGeometry.spaceEmpty(boat, boat.getYaw()) && pointer.world().spawnEntity(boat)) {
            stack.decrement(1);
        }
        cir.setReturnValue(stack);
    }
}
