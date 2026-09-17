package com.xc.longboatlab;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class GiantOars {
    private record Cached(Vec3d position, float yaw, int segments, int width, List<Mounted> mounts,
                          double left, double right, double pitch, double roll, List<Box> boxes, Box envelope) {}
    private static final ThreadLocal<java.util.Map<BoatEntity, Cached>> CACHE =
            ThreadLocal.withInitial(java.util.WeakHashMap::new);
    private GiantOars() {}
    public static int units(ItemStack stack) {
        return Math.max(1, stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("GiantOarUnits"));
    }
    public static boolean giant(ItemStack stack) { return units(stack) > 1; }
    public static double scale(int units) { return Math.cbrt(units); }
    public static double scale(ItemStack stack) {
        double explicit = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getDouble("GiantOarScale");
        return Double.isFinite(explicit) && explicit > 1 && explicit <= Math.cbrt(Integer.MAX_VALUE) ? explicit : scale(units(stack));
    }
    public static void setScale(ItemStack stack, double scale) {
        NbtCompound data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        data.putDouble("GiantOarScale", scale);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
    }
    public static void setData(ItemStack stack, int units, int slot) {
        NbtCompound data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        data.putInt("GiantOarUnits", units);
        if (slot < 0) data.remove("GiantOarSlot"); else data.putInt("GiantOarSlot", slot);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
    }
    public static int slot(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("GiantOarSlot");
    }
    public record Mounted(boolean left, int index, int slot, int units, double scale) {
        public Mounted(boolean left, int index, int slot, int units) { this(left, index, slot, units, GiantOars.scale(units)); }
    }
    public static List<Mounted> mounted(BoatEntity boat) {
        return ((BoatAccess) boat).longboat$giants();
    }
    public static List<Mounted> fromRig(BoatRig rig) {
        List<Mounted> result = new ArrayList<>();
        for (boolean left : new boolean[] {true, false}) {
            OarRack.of(rig.side(left)).specials().forEach((index, stack) -> {
                if (giant(stack)) result.add(new Mounted(left, index, MathHelper.clamp(slot(stack), 0, rig.segments() - 1), units(stack), scale(stack)));
            });
        }
        result.sort(java.util.Comparator.comparing(Mounted::left).thenComparingInt(Mounted::index));
        return List.copyOf(result);
    }
    public static NbtCompound encode(List<Mounted> mounts) {
        NbtList list = new NbtList();
        for (Mounted mount : mounts) {
            NbtCompound tag = new NbtCompound();
            tag.putBoolean("Left", mount.left); tag.putInt("Index", mount.index);
            tag.putInt("Slot", mount.slot); tag.putInt("Units", mount.units); tag.putDouble("Scale", mount.scale); list.add(tag);
        }
        NbtCompound result = new NbtCompound(); result.put("Mounts", list); return result;
    }
    public static List<Mounted> decode(NbtCompound tag) {
        List<Mounted> result = new ArrayList<>();
        var list = tag.getList("Mounts", 10);
        for (int i = 0; i < list.size(); i++) {
            var row = list.getCompound(i);
            int units = Math.max(2, row.getInt("Units"));
            double scale = row.getDouble("Scale");
            if (!Double.isFinite(scale) || scale <= 1 || scale > Math.cbrt(Integer.MAX_VALUE)) scale = scale(units);
            result.add(new Mounted(row.getBoolean("Left"), row.getInt("Index"), row.getInt("Slot"), units, scale));
        }
        return List.copyOf(result);
    }
    public static double phase(BoatEntity boat, boolean left) {
        return ((BoatAccess) boat).longboat$visualState().getDouble(left ? "GiantLeftPhase" : "GiantRightPhase");
    }
    public static BoatGeometry.OarPose pose(BoatEntity boat, Mounted mount, double phase) {
        double size = mount.scale();
        // Grip pivot moves only enough to keep its inboard end resting at the gunwale.
        double x = BoatGeometry.halfWidth(boat) - 0.06 + 0.25 * (size - 1);
        double z = -((BoatAccess) boat).longboat$segments() + 1 + mount.slot * 2.0;
        float sweep = (float) (Math.sin(phase) * 0.38);
        float dip = (float) (-0.32 + Math.cos(phase) * 0.18);
        return new BoatGeometry.OarPose(new Vec3d((mount.left ? 1 : -1) * x, BoatGeometry.HEIGHT + 0.08 * size, z),
                sweep, dip, mount.left ? 1 : -1);
    }
    public static Vec3d point(BoatEntity boat, Mounted mount, double phase, Vec3d local, float yaw) {
        var pose = pose(boat, mount, phase);
        Vec3d v = BoatGeometry.rotateY(BoatGeometry.rotateZ(local.multiply(mount.scale()), pose.dip()), pose.yaw());
        v = new Vec3d(v.x * pose.sign(), v.y, v.z).add(pose.pivot());
        return BoatBody.offset(boat, v, yaw).add(boat.getPos());
    }
    public static List<Box> boxes(BoatEntity boat, Mounted mount, double phase, float yaw) {
        List<Box> result = new ArrayList<>();
        Vec3d pivot = point(boat, mount, phase, Vec3d.ZERO, yaw);
        Vec3d axisX = point(boat, mount, phase, new Vec3d(1, 0, 0), yaw).subtract(pivot);
        Vec3d axisY = point(boat, mount, phase, new Vec3d(0, 1, 0), yaw).subtract(pivot);
        Vec3d axisZ = point(boat, mount, phase, new Vec3d(0, 0, 1), yaw).subtract(pivot);
        int slices = 12; // Fixed complexity, including command-sized paddles.
        for (int i = 0; i < slices; i++) {
            double start = -0.25 + 1.5 * i / slices, end = -0.25 + 1.5 * (i + 1) / slices;
            double width = end > 0.75 ? 0.15 : 0.055;
            double thickness = end > 0.75 ? 0.075 : 0.055;
            double half = (end - start) / 2;
            Vec3d center = pivot.add(axisX.multiply((start + end) / 2));
            // Absolute rotation basis gives the same corner bounds without eight transforms per slice.
            double x = Math.abs(axisX.x) * half + Math.abs(axisY.x) * thickness + Math.abs(axisZ.x) * width;
            double y = Math.abs(axisX.y) * half + Math.abs(axisY.y) * thickness + Math.abs(axisZ.y) * width;
            double z = Math.abs(axisX.z) * half + Math.abs(axisY.z) * thickness + Math.abs(axisZ.z) * width;
            result.add(new Box(center.x - x, center.y - y, center.z - z, center.x + x, center.y + y, center.z + z));
        }
        return result;
    }
    public static List<Box> boxes(BoatEntity boat, float yaw) {
        List<Mounted> mounts = mounted(boat);
        if (mounts.isEmpty()) return List.of();
        double left = phase(boat, true), right = phase(boat, false);
        int segments = ((BoatAccess) boat).longboat$segments();
        Cached old = CACHE.get().get(boat);
        if (old != null && old.position.equals(boat.getPos()) && old.yaw == yaw && old.segments == segments && old.width == ((BoatAccess)boat).longboat$width()
                && old.mounts == mounts && old.left == left && old.right == right
                && old.pitch == BoatBody.pitch(boat) && old.roll == BoatBody.roll(boat)) return old.boxes;
        List<Box> result = new ArrayList<>();
        for (Mounted mount : mounts) result.addAll(boxes(boat, mount, mount.left ? left : right, yaw));
        List<Box> immutable = List.copyOf(result);
        Box envelope = immutable.getFirst();
        for (Box box : immutable) envelope = envelope.union(box);
        CACHE.get().put(boat, new Cached(boat.getPos(), yaw, segments, ((BoatAccess)boat).longboat$width(), mounts, left, right, BoatBody.pitch(boat), BoatBody.roll(boat), immutable, envelope));
        return immutable;
    }
    public static Box discovery(BoatEntity boat) {
        Box envelope=PufferAttachments.bounds(boat);
        if(!mounted(boat).isEmpty()){boxes(boat,boat.getYaw());envelope=envelope.union(CACHE.get().get(boat).envelope);}
        for(var rider:BoatSeats.collisionParts(boat,boat.getYaw()))envelope=envelope.union(rider);
        return envelope;
    }
    public static boolean install(BoatEntity boat, PlayerEntity player, boolean left, Vec3d hit, ItemStack held) {
        BoatAccess access = (BoatAccess) boat;
        BoatRig original = access.longboat$rig();
        if ((original.left().size() > 0 || original.right().size() > 0) && !original.hasGiants()) {
            BoatNoticePayload.send(player, net.minecraft.text.Text.translatable("message.longboatlab.giant_mix")); return false;
        }
        if (access.longboat$abilities().transforming() || access.longboat$visualState().getFloat("Spring") > 0) {
            BoatNoticePayload.send(player, net.minecraft.text.Text.translatable("message.longboatlab.transforming")); return false;
        }
        int slot = MathHelper.clamp((int) Math.floor((hit.z + original.segments()) / 2), 0, original.segments() - 1);
        final int chosen = slot;
        if (fromRig(original).stream().anyMatch(m -> m.left == left && m.slot == chosen)) {
            BoatNoticePayload.send(player, net.minecraft.text.Text.translatable("message.longboatlab.giant_slot")); return false;
        }
        ItemStack installed = held.copyWithCount(1); setData(installed, units(held), slot);
        access.longboat$setRig(original.add(left, installed).withMode(false));
        if (!BoatGeometry.spaceEmpty(boat, boat.getYaw())) {
            access.longboat$setRig(original);
            BoatNoticePayload.send(player, net.minecraft.text.Text.translatable("message.longboatlab.no_space")); return false;
        }
        access.longboat$abilities().giantInstalled(boat, original.compressed());
        return true;
    }
}
