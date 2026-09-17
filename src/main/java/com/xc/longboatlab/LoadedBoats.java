package com.xc.longboatlab;

import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** A world-local spatial index covers long hulls without scanning every boat for every collision query. */
public final class LoadedBoats {
    private record Cells(int x0, int z0, int x1, int z1) {
        boolean large() { return (long) x1 - x0 > 64 || (long) z1 - z0 > 64
                || ((long) x1 - x0 + 1) * ((long) z1 - z0 + 1) > 256; }
    }
    private static final class Index {
        final Map<BoatEntity, Cells> membership = new WeakHashMap<>();
        final Map<Long, Set<BoatEntity>> cells = new HashMap<>();
        final Set<BoatEntity> large = weakSet();
    }
    private static final Map<World, Index> WORLDS = new WeakHashMap<>();
    private LoadedBoats() {}
    private static Set<BoatEntity> weakSet() { return Collections.newSetFromMap(new WeakHashMap<>()); }
    private static Cells cells(Box box) {
        return new Cells(MathHelper.floor(box.minX / 16), MathHelper.floor(box.minZ / 16),
                MathHelper.floor(box.maxX / 16), MathHelper.floor(box.maxZ / 16));
    }
    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }
    public static synchronized void add(Entity entity) {
        if (!(entity instanceof BoatEntity boat)) return;
        Index index = WORLDS.computeIfAbsent(boat.getWorld(), world -> new Index());
        index.membership.putIfAbsent(boat, null);
        update(boat);
    }
    public static synchronized void update(BoatEntity boat) {
        Index index = WORLDS.get(boat.getWorld());
        if (index == null || !index.membership.containsKey(boat)) return;
        Cells next = cells(GiantOars.discovery(boat));
        Cells previous = index.membership.get(boat);
        if (next.equals(previous)) return;
        unlink(index, boat, previous);
        index.membership.put(boat, next);
        if (next.large()) index.large.add(boat);
        else for (int x = next.x0; x <= next.x1; x++) for (int z = next.z0; z <= next.z1; z++)
            index.cells.computeIfAbsent(key(x, z), ignored -> weakSet()).add(boat);
    }
    private static void unlink(Index index, BoatEntity boat, Cells cells) {
        index.large.remove(boat);
        if (cells == null || cells.large()) return;
        for (int x = cells.x0; x <= cells.x1; x++) for (int z = cells.z0; z <= cells.z1; z++) {
            long key = key(x, z);
            Set<BoatEntity> bucket = index.cells.get(key);
            if (bucket != null) { bucket.remove(boat); if (bucket.isEmpty()) index.cells.remove(key); }
        }
    }
    public static synchronized void remove(Entity entity) {
        if (!(entity instanceof BoatEntity boat)) return;
        Index index = WORLDS.get(boat.getWorld());
        if (index != null) unlink(index, boat, index.membership.remove(boat));
    }
    public static synchronized List<BoatEntity> in(World world) {
        Index index = WORLDS.get(world);
        if (index == null) return List.of();
        return index.membership.keySet().stream().filter(boat -> !boat.isRemoved()).toList();
    }
    public static synchronized List<BoatEntity> near(World world, Box box) {
        Index index = WORLDS.get(world);
        if (index == null) return List.of();
        Cells area = cells(box);
        Set<BoatEntity> candidates = Collections.newSetFromMap(new IdentityHashMap<>());
        if (area.large()) candidates.addAll(index.membership.keySet());
        else {
            candidates.addAll(index.large);
            for (int x = area.x0; x <= area.x1; x++) for (int z = area.z0; z <= area.z1; z++) {
                Set<BoatEntity> bucket = index.cells.get(key(x, z));
                if (bucket != null) candidates.addAll(bucket);
            }
        }
        return candidates.stream().filter(boat -> !boat.isRemoved() && GiantOars.discovery(boat).intersects(box)).toList();
    }
}
