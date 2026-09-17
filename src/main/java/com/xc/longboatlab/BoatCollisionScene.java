package com.xc.longboatlab;

import java.util.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/** Per-tick terrain snapshot in small spatial tiles, shared by joints, hull motion and rotation. */
public final class BoatCollisionScene {
    private record Tile(int x, int y, int z) {}
    private record Entry(long tick, BoatCollisionScene scene) {}
    private static final ThreadLocal<Map<BoatEntity, Entry>> CACHE = ThreadLocal.withInitial(WeakHashMap::new);
    private final java.lang.ref.WeakReference<BoatEntity> boatRef;
    private boolean unknown;
    private int queries, denied;
    private final Map<Tile, List<VoxelShape>> tiles = new HashMap<>();
    private static final int TILE = 4, MAX_TILES = 256, MAX_QUERY_TILES = 64;
    private BoatCollisionScene(BoatEntity boat) { this.boatRef = new java.lang.ref.WeakReference<>(boat); }
    public static BoatCollisionScene of(BoatEntity boat) {
        long tick = boat.getWorld().getTime();
        Entry old = CACHE.get().get(boat);
        if (old == null || old.tick != tick) {
            old = new Entry(tick, new BoatCollisionScene(boat)); CACHE.get().put(boat, old);
        }
        return old.scene;
    }
    public static void invalidate(BoatEntity boat) { CACHE.get().remove(boat); }
    public boolean unknown() { return unknown; }
    public static String diagnostics(BoatEntity boat) {
        Entry entry = CACHE.get().get(boat);
        return entry == null ? "No collision snapshot yet" : "tiles="+entry.scene.tiles.size()+"/"+MAX_TILES
                +", queries="+entry.scene.queries+", deferred="+entry.scene.denied;
    }
    private List<VoxelShape> unavailable() { unknown = true; denied++; return null; }
    private List<VoxelShape> shapes(Box region) {
        unknown = false; queries++;
        BoatEntity boat = boatRef.get();
        if (boat == null) return unavailable();
        int x0 = MathHelper.floor(region.minX / TILE), x1 = MathHelper.floor(region.maxX / TILE);
        int y0 = MathHelper.floor(region.minY / TILE), y1 = MathHelper.floor(region.maxY / TILE);
        int z0 = MathHelper.floor(region.minZ / TILE), z1 = MathHelper.floor(region.maxZ / TILE);
        long dx = (long)x1-x0+1, dy = (long)y1-y0+1, dz = (long)z1-z0+1;
        if (dx <= 0 || dy <= 0 || dz <= 0 || dx > MAX_QUERY_TILES || dy > MAX_QUERY_TILES || dz > MAX_QUERY_TILES
                || dx*dy*dz > MAX_QUERY_TILES) return unavailable();
        Set<VoxelShape> result = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) {
            Tile key = new Tile(x, y, z);
            List<VoxelShape> shapes = tiles.get(key);
            if (shapes == null) {
                if (tiles.size() >= MAX_TILES || !com.xc.longboatlab.BoatEnvironment.isLoaded(boat.getWorld(), new BlockPos(x * TILE, y * TILE, z * TILE))) return unavailable();
                Box box = new Box(x * TILE, y * TILE, z * TILE, (x + 1) * TILE, (y + 1) * TILE, (z + 1) * TILE);
                shapes = new ArrayList<>();
                for (VoxelShape shape : boat.getWorld().getBlockCollisions(boat, box)) shapes.add(shape);
                shapes.addAll(boat.getWorld().getEntityCollisions(boat, box));
                if (boat.getWorld().getWorldBorder().canCollide(boat, box)) shapes.add(boat.getWorld().getWorldBorder().asVoxelShape());
                tiles.put(key, shapes);
            }
            for (VoxelShape shape : shapes) if (!shape.isEmpty() && shape.getBoundingBox().intersects(region)) result.add(shape);
        }
        return List.copyOf(result);
    }
    public boolean clear(Box box) {
        Box interior = box.contract(1e-6);
        List<VoxelShape> shapes = shapes(interior);
        if (shapes == null) return false; // Unknown terrain stops motion; never skip collision to meet the work budget.
        if (shapes.isEmpty()) return true;
        var target = VoxelShapes.cuboid(interior);
        for (var shape : shapes) if (VoxelShapes.matchesAnywhere(shape, target, net.minecraft.util.function.BooleanBiFunction.AND)) return false;
        return true;
    }
    public Vec3d clip(Box box, Vec3d motion) {
        List<VoxelShape> shapes = shapes(box.stretch(motion).expand(1e-6));
        if (shapes == null) return Vec3d.ZERO;
        if (shapes.isEmpty()) return motion;
        double y = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, box, shapes, motion.y);
        box = box.offset(0, y, 0);
        double x, z;
        if (Math.abs(motion.x) < Math.abs(motion.z)) {
            z = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, shapes, motion.z);
            x = VoxelShapes.calculateMaxOffset(Direction.Axis.X, box.offset(0, 0, z), shapes, motion.x);
        } else {
            x = VoxelShapes.calculateMaxOffset(Direction.Axis.X, box, shapes, motion.x);
            z = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box.offset(x, 0, 0), shapes, motion.z);
        }
        return new Vec3d(x, y, z);
    }
}
