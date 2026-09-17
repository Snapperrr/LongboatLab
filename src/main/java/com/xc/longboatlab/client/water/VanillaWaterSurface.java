package com.xc.longboatlab.client.water;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;

/** Reads vanilla fluid height without changing blocks, flow, buoyancy or world saves. */
final class VanillaWaterSurface implements WaterSurfaceModel {
    private record Cell(double height, Vec3d flow) {}
    private static final Cell DRY = new Cell(Double.NaN, Vec3d.ZERO);
    private static final Vec3d UP = new Vec3d(0, 1, 0);
    private final Map<Long, Cell> cells = new HashMap<>();
    @Override public void tick(ClientWorld world) { cells.clear(); }
    @Override public void clear() { cells.clear(); }
    private Cell cell(ClientWorld world, BlockPos pos) {
        return cells.computeIfAbsent(pos.asLong(), ignored -> {
            if (!world.isInBuildLimit(pos)) return DRY;
            var fluid = world.getFluidState(pos);
            if (!fluid.isIn(FluidTags.WATER) || world.getFluidState(pos.up()).isIn(FluidTags.WATER)) return DRY;
            if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                    || !world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty()) return DRY;
            return new Cell(pos.getY() + fluid.getHeight(world, pos), fluid.getVelocity(world, pos).multiply(0.014));
        });
    }
    @Override public Surface sample(ClientWorld world, Vec3d probe) {
        BlockPos center = BlockPos.ofFloored(probe);
        if (!com.xc.longboatlab.BoatEnvironment.isLoaded(world, center)) return null;
        for (int dy = 1; dy >= -2; dy--) {
            BlockPos pos = center.add(0, dy, 0);
            Cell cell = cell(world, pos);
            if (cell == DRY || Math.abs(cell.height - probe.y) > 2) continue;
            return new Surface(new Vec3d(probe.x, cell.height, probe.z), UP, cell.flow);
        }
        return null;
    }
}
