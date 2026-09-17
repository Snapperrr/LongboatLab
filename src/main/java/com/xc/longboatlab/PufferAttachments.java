package com.xc.longboatlab;

import java.util.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Sparse per-compartment counts. Layout is deterministic, so folding never changes attachment identity. */
public final class PufferAttachments {
    public record Mount(int segment, int index, PufferGrid.Cell grid) {
        public Mount(int segment,int index){this(segment,index,null);}
        public int face(){return grid==null?(segment>=0?PufferGrid.BOTTOM:PufferGrid.STERN):grid.face();}
        public boolean bottom(){return face()==PufferGrid.BOTTOM;}
    }
    private static final List<Vec3d> CELLS;
    static {
        List<Vec3d> cells = new ArrayList<>();
        for (int z = -2; z <= 2; z++) for (int x = -1; x <= 1; x++) cells.add(new Vec3d(x * 0.34, 0, z * 0.34));
        cells.sort(Comparator.<Vec3d>comparingDouble(v -> Math.max(Math.abs(v.x), Math.abs(v.z)))
                .thenComparingDouble(v -> Math.atan2(v.z, v.x)));
        CELLS = List.copyOf(cells);
    }
    private PufferAttachments() {}
    public static Vec3d cell(int index) { return CELLS.get(index % 15); }
    public static int count(Map<Integer, Integer> counts) { return (int) Math.min(Integer.MAX_VALUE, counts.values().stream().mapToLong(Integer::longValue).sum()); }
    public static Map<Integer, Integer> sanitize(Map<Integer, Integer> source, int segments, int total) {
        TreeMap<Integer, Integer> next = new TreeMap<>();
        long remaining = total;
        for (var entry : new TreeMap<>(source).entrySet()) {
            if (entry.getKey() < 0 || entry.getKey() >= segments || entry.getValue() <= 0) continue;
            int value = (int) Math.min(remaining, entry.getValue());
            if (value > 0) next.put(entry.getKey(), value);
            remaining -= value;
        }
        return Collections.unmodifiableMap(next);
    }
    public static NbtCompound encode(Map<Integer, Integer> source) {
        NbtCompound tag = new NbtCompound();
        source.forEach((segment, count) -> tag.putInt(Integer.toString(segment), count));
        return tag;
    }
    public static Map<Integer, Integer> decode(NbtCompound tag) {
        TreeMap<Integer, Integer> values = new TreeMap<>();
        for (String key : tag.getKeys()) {
            try {
                int segment = Integer.parseInt(key), count = tag.getInt(key);
                if (segment >= 0 && count > 0) values.put(segment, count);
            } catch (NumberFormatException ignored) { }
        }
        return Collections.unmodifiableMap(values);
    }
    public static int sternCount(BoatEntity boat) {
        BoatAccess access = (BoatAccess) boat;
        return Math.max(0, access.longboat$puffers() - count(access.longboat$bottomPuffers()) - PufferGrid.count(access.longboat$gridPuffers()));
    }
    public static int segmentAt(BoatEntity boat, Vec3d local) {
        var access = (BoatAccess) boat;
        return MathHelper.clamp((int) Math.floor((local.z + access.longboat$segments()) / 2), 0, access.longboat$segments() - 1);
    }
    private static long layersBefore(BoatEntity boat, int segment) {
        long layers = 0;
        for (var entry : ((BoatAccess) boat).longboat$bottomPuffers().entrySet()) {
            if (entry.getKey() >= segment) break;
            layers += (entry.getValue() + 14L) / 15;
        }
        return layers;
    }
    public static Vec3d direction(Mount mount) { return PufferGrid.normal(mount.face()); }
    public static Vec3d position(BoatEntity boat, Mount mount, float extension) {
        if(mount.grid()!=null)return PufferGrid.position(boat,mount.grid(),mount.index(),extension);
        int segments = ((BoatAccess) boat).longboat$segments();
        if (!mount.bottom()) {
            int row = mount.index / 4, column = mount.index % 4;
            int columns = Math.min(4, sternCount(boat) - row * 4);
            return new Vec3d((column - (columns - 1) / 2.0) * 0.31, 0.25 + row * 0.23,
                    -(1 + (segments - 1) * (double) extension) - 0.095);
        }
        Vec3d cell = CELLS.get(mount.index % 15);
        double extraLayers = (1 - extension) * layersBefore(boat, mount.segment);
        return new Vec3d(cell.x, -0.14 - (mount.index / 15 + extraLayers) * 0.24,
                cell.z + (-segments + 1 + mount.segment * 2.0) * extension);
    }
    public static Vec3d mouth(BoatEntity boat, Mount mount, float extension) {
        return position(boat, mount, extension).add(direction(mount).multiply(0.20));
    }
    /** Query only rows near a point; enormous command counts do not cause enormous per-frame loops. */
    public static List<Mount> near(BoatEntity boat, Vec3d local, double radius, float extension) {
        List<Mount> result = new ArrayList<>();
        int stern = sternCount(boat);
        double half = 1 + (((BoatAccess) boat).longboat$segments() - 1) * (double) extension;
        if (Math.abs(local.z + half) <= radius + 1 && Math.abs(local.x) <= radius + 1) {
            long first = Math.max(0, (long) Math.floor((local.y - radius - 0.25) / 0.23));
            long last = Math.min((stern + 3L) / 4, (long) Math.ceil((local.y + radius - 0.25) / 0.23) + 1);
            for (long i = first * 4; i < Math.min(stern, last * 4); i++) result.add(new Mount(-1, (int) i));
        }
        long layers = 0;
        int segments = ((BoatAccess) boat).longboat$segments();
        for (var entry : ((BoatAccess) boat).longboat$bottomPuffers().entrySet()) {
            double z = (-segments + 1 + entry.getKey() * 2.0) * extension;
            double top = -0.14 - (1 - extension) * layers * 0.24;
            long rows = (entry.getValue() + 14L) / 15;
            layers += rows;
            if (Math.abs(local.z - z) > radius + 1 || Math.abs(local.x) > radius + 1) continue;
            long first = Math.max(0, (long) Math.floor((top - local.y - radius) / 0.24));
            long last = Math.min(rows, (long) Math.ceil((top - local.y + radius) / 0.24) + 1);
            for (long i = first * 15; i < Math.min(entry.getValue(), last * 15); i++) result.add(new Mount(entry.getKey(), (int) i));
        }
        PufferGrid.near(boat,local,radius,extension,result);
        return result;
    }
    public static List<Mount> preview(BoatEntity boat) {
        List<Mount> mounts = new ArrayList<>();
        // Full ship dimensions stay visible; only densely subpixel fish use deterministic samples in item previews.
        int count = sternCount(boat);
        long stride = Math.max(1, (count + 127L) / 128);
        for (long i = 0; i < count; i += stride) mounts.add(new Mount(-1, (int) i));
        ((BoatAccess) boat).longboat$bottomPuffers().forEach((segment, n) -> {
            long step = Math.max(1, (n + 127L) / 128);
            for (long i = 0; i < n; i += step) mounts.add(new Mount(segment, (int) i));
        });
        ((BoatAccess)boat).longboat$gridPuffers().forEach((cell,n)->{
            long step=Math.max(1,(n+127L)/128); for(long i=0;i<n;i+=step)mounts.add(new Mount(cell.slot(),(int)i,cell));
        });
        return mounts;
    }
    public static Box bounds(BoatEntity boat) {
        if (((BoatAccess) boat).longboat$puffers() == 0) return BoatBody.box(boat,
                new Box(-BoatGeometry.halfWidth(boat), 0, -BoatGeometry.halfLength(boat), BoatGeometry.halfWidth(boat), BoatGeometry.HEIGHT+BoatGeometry.stackHeight(boat), BoatGeometry.halfLength(boat)), boat.getYaw());
        long layers = 0;
        for (int n : ((BoatAccess) boat).longboat$bottomPuffers().values()) layers += (n + 14L) / 15;
        double half = BoatGeometry.halfLength(boat);
        Box local = new Box(-BoatGeometry.halfWidth(boat)-0.17, -layers * 0.24 - 0.4, -half - 0.5, BoatGeometry.halfWidth(boat)+0.17,
                Math.max(BoatGeometry.HEIGHT + BoatGeometry.stackHeight(boat), (sternCount(boat)+3L)/4*0.23+0.5), half+0.2);
        for(var e:((BoatAccess)boat).longboat$gridPuffers().entrySet()) {
            Vec3d first=PufferGrid.position(boat,e.getKey(),0,BoatGeometry.extension(boat));
            Vec3d last=PufferGrid.position(boat,e.getKey(),e.getValue()-1,BoatGeometry.extension(boat));
            local=local.union(new Box(first,last).expand(0.3));
        }
        return BoatBody.box(boat, local, boat.getYaw());
    }
}
