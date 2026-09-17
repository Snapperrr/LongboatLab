package com.xc.longboatlab.mixin;

import com.xc.longboatlab.BoatAbilities;
import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.BoatRig;
import com.xc.longboatlab.BoatSeats;
import com.xc.longboatlab.GiantOars;
import com.xc.longboatlab.BoatEnvironment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends VehicleEntity implements BoatAccess {
    @Unique private static final TrackedData<Integer> LONGBOAT_WIDTH = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private static final TrackedData<Integer> LONGBOAT_SEGMENTS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private static final TrackedData<Integer> LONGBOAT_LEFT = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private static final TrackedData<Integer> LONGBOAT_RIGHT = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private static final TrackedData<Boolean> LONGBOAT_COMPRESSED = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique private static final TrackedData<Integer> LONGBOAT_PUFFERS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private static final TrackedData<NbtCompound> LONGBOAT_ABILITIES = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    @Unique private static final TrackedData<NbtCompound> LONGBOAT_SEATS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    @Unique private static final TrackedData<NbtCompound> LONGBOAT_GIANTS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    @Unique private static final TrackedData<NbtCompound> LONGBOAT_BOTTOM_PUFFERS = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    @Unique private static final TrackedData<NbtCompound> LONGBOAT_GRID = DataTracker.registerData(BoatEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    @Unique private NbtCompound longboat$gridCache;
    @Unique private java.util.Map<com.xc.longboatlab.PufferGrid.Cell,Integer> longboat$grid = java.util.Map.of();
    @Override public java.util.Map<com.xc.longboatlab.PufferGrid.Cell,Integer> longboat$gridPuffers() {
        var tag=dataTracker.get(LONGBOAT_GRID);
        if(tag!=longboat$gridCache){longboat$gridCache=tag;longboat$grid=com.xc.longboatlab.PufferGrid.decode(tag);}
        return longboat$grid;
    }
    @Unique private NbtCompound longboat$bottomCache;
    @Unique private java.util.Map<Integer, Integer> longboat$bottom = java.util.Map.of();
    @Unique private NbtCompound longboat$giantCache;
    @Unique private java.util.List<GiantOars.Mounted> longboat$mounts = java.util.List.of();
    @Unique private BoatRig longboat$rig = BoatRig.vanilla();
    @Unique private final BoatAbilities longboat$abilities = new BoatAbilities();
    @Unique private double longboat$previousVerticalVelocity;
    @Unique private float longboat$clientPreviousYaw;
    @Shadow private float yawVelocity;
    @Shadow private BoatEntity.Location location;
    @Shadow private double waterLevel;
    @Shadow private int lerpTicks;

    protected BoatEntityMixin(EntityType<?> type, World world) { super(type, world); }

    @Inject(method = "checkBoatInWater", at = @At("HEAD"), cancellable = true)
    private void longboat$waterFootprint(CallbackInfoReturnable<Boolean> cir) {
        BoatEntity boat = (BoatEntity) (Object) this;
        if (BoatGeometry.visibleSegments(boat) <= 1 && longboat$width() == 1) return;
        waterLevel = BoatEnvironment.waterLevel(boat, getY());
        cir.setReturnValue(getY() < waterLevel);
    }

    @Inject(method = "getUnderWaterLocation", at = @At("HEAD"), cancellable = true)
    private void longboat$submergedFootprint(CallbackInfoReturnable<BoatEntity.Location> cir) {
        BoatEntity boat = (BoatEntity) (Object) this;
        if (BoatGeometry.visibleSegments(boat) > 1 || longboat$width() > 1) cir.setReturnValue(BoatEnvironment.underwater(boat));
    }

    @Inject(method = "getNearbySlipperiness", at = @At("HEAD"), cancellable = true)
    private void longboat$groundFootprint(CallbackInfoReturnable<Float> cir) {
        BoatEntity boat = (BoatEntity) (Object) this;
        if (BoatGeometry.visibleSegments(boat) > 1 || longboat$width() > 1) cir.setReturnValue(BoatEnvironment.slipperiness(boat));
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void longboat$track(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(LONGBOAT_WIDTH, 1).add(LONGBOAT_SEGMENTS, 1).add(LONGBOAT_LEFT, 1).add(LONGBOAT_RIGHT, 1)
                .add(LONGBOAT_COMPRESSED, false).add(LONGBOAT_PUFFERS, 0).add(LONGBOAT_ABILITIES, new NbtCompound())
                .add(LONGBOAT_GRID, new NbtCompound()).add(LONGBOAT_BOTTOM_PUFFERS, new NbtCompound()).add(LONGBOAT_SEATS, new NbtCompound()).add(LONGBOAT_GIANTS, new NbtCompound());
    }

    @Override public java.util.List<GiantOars.Mounted> longboat$giants() {
        NbtCompound current = dataTracker.get(LONGBOAT_GIANTS);
        if (current != longboat$giantCache) { longboat$giantCache = current; longboat$mounts = GiantOars.decode(current); }
        return longboat$mounts;
    }
    @Override public int longboat$width() { return dataTracker.get(LONGBOAT_WIDTH); }
    @Override public int longboat$segments() { return dataTracker.get(LONGBOAT_SEGMENTS); }
    @Override public int longboat$oars(boolean left) { return dataTracker.get(left ? LONGBOAT_LEFT : LONGBOAT_RIGHT); }
    @Override public boolean longboat$compressed() { return dataTracker.get(LONGBOAT_COMPRESSED); }
    @Override public java.util.Map<Integer, Integer> longboat$bottomPuffers() {
        NbtCompound tag = dataTracker.get(LONGBOAT_BOTTOM_PUFFERS);
        if (tag != longboat$bottomCache) { longboat$bottomCache = tag; longboat$bottom = com.xc.longboatlab.PufferAttachments.decode(tag); }
        return longboat$bottom;
    }
    @Override public int longboat$puffers() { return dataTracker.get(LONGBOAT_PUFFERS); }
    @Override public BoatRig longboat$rig() { return longboat$rig; }
    @Override public BoatAbilities longboat$abilities() { return longboat$abilities; }
    @Override public NbtCompound longboat$visualState() { return dataTracker.get(LONGBOAT_ABILITIES); }
    @Override public void longboat$syncAbilities() { dataTracker.set(LONGBOAT_ABILITIES, longboat$abilities.visualState()); }
    @Override public NbtCompound longboat$seatData() { return dataTracker.get(LONGBOAT_SEATS); }
    @Override public void longboat$setSeatData(NbtCompound seats) { dataTracker.set(LONGBOAT_SEATS, seats.copy()); }
    @Override public void longboat$setRig(BoatRig rig) {
        longboat$rig = rig;
        dataTracker.set(LONGBOAT_GIANTS, GiantOars.encode(GiantOars.fromRig(rig)));
        dataTracker.set(LONGBOAT_SEGMENTS, rig.segments());
        dataTracker.set(LONGBOAT_WIDTH, rig.width());
        dataTracker.set(LONGBOAT_LEFT, rig.left().size());
        dataTracker.set(LONGBOAT_RIGHT, rig.right().size());
        dataTracker.set(LONGBOAT_COMPRESSED, rig.compressed());
        dataTracker.set(LONGBOAT_PUFFERS, rig.puffers());
        dataTracker.set(LONGBOAT_BOTTOM_PUFFERS, com.xc.longboatlab.PufferAttachments.encode(rig.bottomPuffers()));
        dataTracker.set(LONGBOAT_GRID, com.xc.longboatlab.PufferGrid.encode(rig.gridPuffers()));
        setBoundingBox(BoatGeometry.bounds((BoatEntity) (Object) this));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void longboat$save(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(BoatRig.KEY, longboat$rig.toNbt(getRegistryManager()));
        nbt.put("LongboatSeats", longboat$seatData().copy());
        nbt.put("LongboatJoints", longboat$abilities.saveJoints());
        nbt.put("LongboatBody", longboat$abilities.body().save());
        nbt.putBoolean("LongboatSpringFlight", longboat$abilities.springFlight());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void longboat$load(NbtCompound nbt, CallbackInfo ci) {
        longboat$abilities.loadJoints(nbt.getCompound("LongboatJoints"));
        longboat$abilities.body().load(nbt.getCompound("LongboatBody"));
        longboat$abilities.restoreSpringFlight(nbt.getBoolean("LongboatSpringFlight"));
        longboat$syncAbilities();
        longboat$setRig(nbt.contains(BoatRig.KEY, NbtElement.COMPOUND_TYPE)
                ? BoatRig.fromNbt(nbt.getCompound(BoatRig.KEY), getRegistryManager()) : BoatRig.vanilla());
        longboat$setSeatData(nbt.getCompound("LongboatSeats"));
    }

    @Inject(method = "getPickBlockStack", at = @At("RETURN"))
    private void longboat$pick(CallbackInfoReturnable<ItemStack> cir) {
        BoatRig rig = getWorld().isClient ? BoatGeometry.previewRig(this) : longboat$rig;
        rig.applyToStack(cir.getReturnValue(), getRegistryManager());
    }

    @Inject(method = "updatePaddles", at = @At("HEAD"), cancellable = true)
    private void longboat$noClientThrust(CallbackInfo ci) { ci.cancel(); }

    @Inject(method = "tick", at = @At("HEAD"))
    private void longboat$abilitiesTick(CallbackInfo ci) {
        if (!getWorld().isClient) {
            BoatSeats.ensureAssigned((BoatEntity)(Object)this);
            longboat$abilities.tick((BoatEntity) (Object) this);
            yawVelocity = longboat$abilities.turnVelocity();
        } else longboat$clientPreviousYaw = getYaw();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void longboat$afterMove(CallbackInfo ci) {
        if (!getWorld().isClient) longboat$abilities.afterMove((BoatEntity) (Object) this);
        else yawVelocity = MathHelper.wrapDegrees(getYaw() - longboat$clientPreviousYaw);
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void longboat$rememberVertical(CallbackInfo ci) {
        longboat$previousVerticalVelocity = getVelocity().y;
        // Vanilla's fully submerged states intentionally sink boats, including under flowing water.
        // Use the same physical hull buoyancy as a tilted boat instead of switching back to sinking.
        boolean underwater = location == BoatEntity.Location.UNDER_WATER
                || location == BoatEntity.Location.UNDER_FLOWING_WATER;
        if (!getWorld().isClient && (longboat$abilities.locked() || longboat$abilities.springFlight()
                || underwater || com.xc.longboatlab.BoatBody.tilted((BoatEntity) (Object) this))) {
            if (!longboat$abilities.locked()) {
                longboat$abilities.body().environment((BoatEntity) (Object) this);
                // Cancelling vanilla buoyancy also skips its yaw damping; retain steering inertia decay.
                yawVelocity *= isOnGround() ? 0.5f : 0.9f;
            }
            longboat$abilities.drive((BoatEntity) (Object) this, yawVelocity);
            yawVelocity = longboat$abilities.turnVelocity();
            ci.cancel();
        }
    }

    @Inject(method = "updateVelocity", at = @At("TAIL"))
    private void longboat$physics(CallbackInfo ci) {
        if (getWorld().isClient) return;
        // Target 0.18 blocks of draft, instead of submerging almost the entire hull.
        if (location == BoatEntity.Location.IN_WATER && !longboat$abilities.protectsFall()) {
            double y = MathHelper.clamp((waterLevel - getY() - 0.18) * 0.2
                    + longboat$previousVerticalVelocity * 0.55, -0.12, 0.12);
            setVelocity(getVelocity().x, y, getVelocity().z);
        }
        longboat$abilities.drive((BoatEntity) (Object) this, yawVelocity);
        yawVelocity = longboat$abilities.turnVelocity();
    }

    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void longboat$canBoard(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(getPassengerList().size()<BoatSeats.capacity((BoatEntity)(Object)this)
                && !isSubmergedIn(net.minecraft.registry.tag.FluidTags.WATER));
    }

    @Inject(method = "clampPassengerYaw", at = @At("HEAD"), cancellable = true)
    private void longboat$freeLook(Entity passenger, CallbackInfo ci) {
        if (passenger instanceof net.minecraft.entity.player.PlayerEntity) {
            passenger.setBodyYaw(passenger.getYaw());
            ci.cancel();
        }
    }

    @Inject(method = "getMaxPassengers", at = @At("HEAD"), cancellable = true)
    private void longboat$passengerCapacity(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(BoatSeats.capacity((BoatEntity)(Object)this));
    }

    @Inject(method = "updatePassengerForDismount", at = @At("RETURN"), cancellable = true)
    private void longboat$safeExit(net.minecraft.entity.LivingEntity passenger, CallbackInfoReturnable<Vec3d> cir) {
        Vec3d safe = BoatSeats.dismount((BoatEntity) (Object) this, passenger, cir.getReturnValue());
        cir.setReturnValue(safe);
    }

    @Inject(method = "getPassengerAttachmentPos", at = @At("RETURN"), cancellable = true)
    private void longboat$stackSeat(Entity passenger, EntityDimensions dimensions, float scale,
                                     CallbackInfoReturnable<Vec3d> cir) {
        cir.setReturnValue(BoatSeats.attachment((BoatEntity) (Object) this, passenger, cir.getReturnValue()));
    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At("TAIL"))
    private void longboat$smoothServerMotion(double x, double y, double z, float yaw, float pitch, int steps, CallbackInfo ci) {
        lerpTicks = 3;
    }

    @Inject(method = "fall", at = @At("HEAD"), cancellable = true)
    private void longboat$fall(double heightDifference, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
        if (longboat$abilities.protectsFall()) {
            fallDistance = 0;
            ci.cancel();
            return;
        }
        if (onGround && fallDistance > 3 && !hasVehicle() && location == BoatEntity.Location.ON_LAND) {
            if (!getWorld().isClient && !isRemoved()) killAndDropItem(((BoatEntity) (Object) this).asItem());
            fallDistance = 0;
            ci.cancel();
        }
    }
}
