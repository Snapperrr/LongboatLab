package com.xc.longboatlab;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Probe the footprint instead of the empty rectangle surrounding a diagonal longboat. */
public final class BoatEnvironment {
    private BoatEnvironment() {}

    /** Query already available chunks without the deprecated WorldView bridge or synchronous loading. */
    public static boolean isLoaded(net.minecraft.world.World world, BlockPos pos) {
        return world.getChunkManager().getWorldChunk(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    /** Local immersion across voxel boundaries; an internal water-block face is not a free surface. */
    public static double immersionAt(BoatEntity boat, Vec3d probe, double offset, double depth) {
        var world = boat.getWorld();
        BlockPos pos = BlockPos.ofFloored(probe);
        if (!com.xc.longboatlab.BoatEnvironment.isLoaded(world, pos)) return 0;
        var fluid = world.getFluidState(pos);
        if (!fluid.isIn(FluidTags.WATER)) return 0;
        double surfaceY = pos.getY() + fluid.getHeight(world, pos);
        BlockPos above = pos.up();
        var upperFluid = world.getFluidState(above);
        if (upperFluid.isIn(FluidTags.WATER)) {
            // All callers integrate less than one block: one neighbor suffices even in a deep ocean.
            // Keep shallow flowing layers fractional instead of granting full buoyancy on contact.
            surfaceY = above.getY() + upperFluid.getHeight(world, above);
        }
        return MathHelper.clamp((surfaceY - probe.y + offset) / depth, 0, 1);
    }

    private static Set<BlockPos> surface(BoatEntity boat, double y) {
        Set<BlockPos> positions = new HashSet<>();
        int blockY = MathHelper.floor(y);
        if (BoatGeometry.halfLength(boat) * BoatGeometry.halfWidth(boat) <= 128) {
            for (var part : BoatGeometry.hullParts(boat, boat.getYaw())) {
                for (int x = MathHelper.floor(part.minX); x < MathHelper.ceil(part.maxX); x++)
                    for (int z = MathHelper.floor(part.minZ); z < MathHelper.ceil(part.maxZ); z++)
                        positions.add(new BlockPos(x, blockY, z));
            }
        } else {
            // Extremely long debug hulls use distributed buoyancy samples, including both ends.
            double half = BoatGeometry.halfLength(boat) - 0.1;
            double angle = Math.toRadians(boat.getYaw());
            for (int i = 0; i <= 128; i++) for (int side = -1; side <= 1; side++) {
                Vec3d point = boat.getPos().add(BoatGeometry.rotateY(new Vec3d(side * (BoatGeometry.halfWidth(boat)-0.0875), 0, -half + 2 * half * i / 128), angle));
                positions.add(BlockPos.ofFloored(point.x, y, point.z));
            }
        }
        return positions;
    }
    public static double waterLevel(BoatEntity boat, double y) {
        double level = -Double.MAX_VALUE;
        var world = boat.getWorld();
        for (BlockPos pos : surface(boat, y)) {
            if (!com.xc.longboatlab.BoatEnvironment.isLoaded(world, pos)) continue;
            var fluid = world.getFluidState(pos);
            if (fluid.isIn(FluidTags.WATER)) level = Math.max(level, pos.getY() + fluid.getHeight(world, pos));
        }
        return level;
    }
    public static BoatEntity.Location underwater(BoatEntity boat) {
        double top = boat.getBoundingBox().maxY + 0.001;
        boolean still = false;
        var world = boat.getWorld();
        for (BlockPos pos : surface(boat, top)) {
            if (!com.xc.longboatlab.BoatEnvironment.isLoaded(world, pos)) continue;
            var fluid = world.getFluidState(pos);
            if (fluid.isIn(FluidTags.WATER) && top < pos.getY() + fluid.getHeight(world, pos)) {
                if (!fluid.isStill()) return BoatEntity.Location.UNDER_FLOWING_WATER;
                still = true;
            }
        }
        return still ? BoatEntity.Location.UNDER_WATER : null;
    }
    public static float slipperiness(BoatEntity boat) {
        double sum = 0;
        int count = 0;
        var world = boat.getWorld();
        for (BlockPos pos : surface(boat, boat.getY() - 0.001)) {
            if (!com.xc.longboatlab.BoatEnvironment.isLoaded(world, pos)) continue;
            var state = world.getBlockState(pos);
            var shape = state.getCollisionShape(world, pos);
            if (!shape.isEmpty() && shape.getBoundingBox().maxY + pos.getY() >= boat.getY() - 0.002) {
                sum += state.getBlock().getSlipperiness();
                count++;
            }
        }
        return count == 0 ? 0 : (float) (sum / count);
    }
}
