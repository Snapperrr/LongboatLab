package com.xc.longboatlab;

import java.util.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.*;

/** Sparse columns on five hull faces. Counts are layers in a chosen cell, never a placement order. */
public final class PufferGrid {
    public static final int BOTTOM=0, STERN=1, BOW=2, LEFT=3, RIGHT=4, FACES=5;
    public record Cell(int face, int slot, int u, int v, int lane, int depth, boolean outward) {
        public Cell(int face,int slot,int u,int v) { this(face,slot,u,v,0,0,false); }
        public Cell moved(int slot,int lane) { return new Cell(face,slot,u,v,lane,depth,outward); }
    }
    private PufferGrid() {}
    public static Vec3d normal(int face) {
        return switch(face) {
            case BOTTOM -> new Vec3d(0,-1,0); case STERN -> new Vec3d(0,0,-1);
            case BOW -> new Vec3d(0,0,1); case LEFT -> new Vec3d(1,0,0);
            default -> new Vec3d(-1,0,0);
        };
    }
    public static Vec3d layerStep(int face) {
        // End-mounted fish stay on the end plane, preserving the close-to-stern layout.
        return face==STERN||face==BOW ? new Vec3d(0,0.46,0) : normal(face).multiply(0.24);
    }
    public static Vec3d layerStep(Cell c) { return c.outward ? normal(c.face).multiply(0.32) : layerStep(c.face); }
    public static int count(Map<Cell,Integer> cells) { return (int)Math.min(Integer.MAX_VALUE, cells.values().stream().mapToLong(Integer::longValue).sum()); }
    public static Map<Cell,Integer> sanitize(Map<Cell,Integer> source, int segments, int budget) { return sanitize(source,segments,1,budget); }
    public static Map<Cell,Integer> sanitize(Map<Cell,Integer> source, int segments, int width, int budget) {
        Map<Cell,Integer> result=new LinkedHashMap<>(); long remaining=budget;
        for (var e:source.entrySet()) {
            Cell c=e.getKey();
            if (c.face<0 || c.face>=FACES || c.slot<0 || c.slot>=segments || c.lane<0 || c.lane>=width || c.depth<0 || e.getValue()<=0
                    || Math.abs((long)c.u)>(c.face>=LEFT?3:2) || (c.face==BOTTOM ? Math.abs((long)c.v)>3 : c.v<0)) continue;
            int n=(int)Math.min(remaining,e.getValue());
            if(n>0) result.put(c,n); remaining-=n;
        }
        return Collections.unmodifiableMap(result);
    }
    public static NbtCompound encode(Map<Cell,Integer> cells) {
        NbtCompound tag=new NbtCompound(); NbtList rows=new NbtList();
        cells.forEach((c,n)->{ NbtCompound row=new NbtCompound(); row.putInt("Face",c.face); row.putInt("Slot",c.slot);
            row.putInt("Lane",c.lane); row.putInt("Depth",c.depth); row.putBoolean("Outward",c.outward); row.putInt("U",c.u); row.putInt("V",c.v); row.putInt("Count",n); rows.add(row); });
        tag.put("Cells",rows); return tag;
    }
    public static Map<Cell,Integer> decode(NbtCompound tag) {
        Map<Cell,Integer> cells=new LinkedHashMap<>(); var rows=tag.getList("Cells",NbtElement.COMPOUND_TYPE);
        for(int i=0;i<rows.size();i++){var r=rows.getCompound(i); cells.put(new Cell(r.getInt("Face"),r.getInt("Slot"),r.getInt("U"),r.getInt("V"),r.getInt("Lane"),r.getInt("Depth"),r.getBoolean("Outward")),r.getInt("Count"));}
        return Collections.unmodifiableMap(cells);
    }
    public static Vec3d position(BoatEntity boat, Cell c, int layer, float extension) {
        int length=((BoatAccess)boat).longboat$segments(); double half=1+(length-1)*(double)extension;
        double z=(-length+1+c.slot*2.0)*extension;
        double x=BoatGeometry.laneX(c.lane,((BoatAccess)boat).longboat$width())+gridOffset(c.u,false);
        double depth=c.face==BOTTOM || c.face>=LEFT ? c.slot*(1-extension)*0.24:0;
        Vec3d base=switch(c.face) {
            case BOTTOM -> new Vec3d(x,-0.14,gridOffset(c.v,true)+z);
            case STERN -> new Vec3d(x,0.17+c.v*0.23,-half-0.14);
            case BOW -> new Vec3d(x,0.17+c.v*0.23,half+0.14);
            case LEFT -> new Vec3d(BoatGeometry.halfWidth(boat)+0.14,0.17+c.v*0.23,z+gridOffset(c.u,true));
            default -> new Vec3d(-BoatGeometry.halfWidth(boat)-0.14,0.17+c.v*0.23,z+gridOffset(c.u,true));
        };
        return base.add(normal(c.face).multiply(depth+c.depth*0.32)).add(layerStep(c).multiply(layer));
    }
    // Preserve every old grid position; add one shared row at each compartment/lane boundary.
    static double gridOffset(int index, boolean longitudinal) {
        int edge=longitudinal?3:2;
        return Math.abs(index)==edge ? Math.copySign(longitudinal?1:BoatGeometry.HALF_WIDTH,index) : index*0.34;
    }
    private static int gridIndex(double offset, boolean longitudinal) {
        int edge=longitudinal?3:2, best=0; double distance=Double.POSITIVE_INFINITY;
        for(int i=-edge;i<=edge;i++) {
            double d=Math.abs(offset-gridOffset(i,longitudinal));
            if(d<distance) { distance=d;best=i; }
        }
        return best;
    }
    public static Cell canonical(BoatEntity boat,Cell c) {
        // Only extended neighbors share a seam. Folding separates their rows in depth and
        // overlays their compartments; remapping +3 to the next -3 would jump to the other end.
        boolean sharedSeam=BoatGeometry.extension(boat)>=0.9999f;
        BoatAccess access=(BoatAccess)boat;
        int slot=c.slot,lane=c.lane,u=c.u,v=c.v;
        if(sharedSeam && c.face>=LEFT && u==3 && slot<access.longboat$segments()-1) { slot++;u=-3; }
        if(sharedSeam && c.face==BOTTOM && v==3 && slot<access.longboat$segments()-1) { slot++;v=-3; }
        if(c.face<LEFT && u==2 && lane<access.longboat$width()-1) { lane++;u=-2; }
        return new Cell(c.face,slot,u,v,lane,c.depth,c.outward);
    }
    public static Cell aimed(BoatEntity boat, Vec3d hit) {
        double half=BoatGeometry.halfLength(boat); int face;
        if(hit.y<=1e-4) face=BOTTOM;
        else if(hit.z<=-half+0.16) face=STERN;
        else if(hit.z>=half-0.16) face=BOW;
        else if(Math.abs(hit.x)>=BoatGeometry.halfWidth(boat)-0.16) face=hit.x>0?LEFT:RIGHT;
        else return null;
        int length=((BoatAccess)boat).longboat$segments();
        int slot=((BoatAccess)boat).longboat$compressed() ? (length-1)/2 : PufferAttachments.segmentAt(boat,hit);
        double z=hit.z-(-length+1+slot*2.0)*BoatGeometry.extension(boat);
        int lane=face>=LEFT?0:BoatGeometry.laneAt(boat,hit.x);
        double x=hit.x-BoatGeometry.laneX(lane,((BoatAccess)boat).longboat$width());
        int u=gridIndex(face>=LEFT?z:x,face>=LEFT);
        int v=face==BOTTOM ? gridIndex(z,true) : MathHelper.clamp((int)Math.round((hit.y-0.17)/0.23),0,1);
        return canonical(boat,new Cell(face,face==STERN||face==BOW?0:slot,u,v,lane,0,true));
    }
    public static Cell placement(BoatGeometry.Target target) {
        BoatEntity boat=target.boat(); var mount=target.puffer();
        if(mount==null) return aimed(boat,target.localHit());
        Cell c=mount.grid();
        if(c==null) {
            Vec3d at=PufferAttachments.position(boat,mount,BoatGeometry.extension(boat));
            int lane=BoatGeometry.laneAt(boat,at.x);
            c=new Cell(mount.face(),Math.max(0,mount.segment()),
                    MathHelper.clamp((int)Math.round((at.x-BoatGeometry.laneX(lane,((BoatAccess)boat).longboat$width()))/0.34),-1,1),
                    mount.bottom()?0:Math.max(0,(int)Math.round((at.y-0.17)/0.23)),lane,0,true);
            return new Cell(c.face,c.slot,c.u,c.v,c.lane,1,true);
        }
        Vec3d hit=target.localHit().subtract(PufferAttachments.position(boat,mount,BoatGeometry.extension(boat)));
        if(c.face!=BOTTOM && hit.y>=0.159) {
            // Aim at the top to grow upwards; aiming at the face extends the horizontal branch.
            long v=(long)c.v+2+(c.outward || c.face>=LEFT ? 0 : mount.index()*2L);
            long depth=(long)c.depth+(c.outward?mount.index():c.face>=LEFT?Math.round(mount.index()*0.75):0);
            if(v>Integer.MAX_VALUE||depth>Integer.MAX_VALUE)return null;
            return new Cell(c.face,c.slot,c.u,(int)v,c.lane,(int)depth,true);
        }
        if(c.outward || c.face>=LEFT || c.face==BOTTOM) return c;
        long v=(long)c.v+mount.index()*2L;
        if(v>Integer.MAX_VALUE||c.depth==Integer.MAX_VALUE)return null;
        return new Cell(c.face,c.slot,c.u,(int)v,c.lane,c.depth+1,true);
    }
    public static PufferAttachments.Mount preview(BoatGeometry.Target target) {
        BoatEntity boat=target.boat(); Cell cell=placement(target); if(cell==null)return null;
        return new PufferAttachments.Mount(cell.slot,((BoatAccess)boat).longboat$gridPuffers().getOrDefault(cell,0),cell);
    }
    public static void near(BoatEntity boat, Vec3d local, double radius, float extension, List<PufferAttachments.Mount> result) {
        ((BoatAccess)boat).longboat$gridPuffers().forEach((c,n)->{
            Vec3d origin=position(boat,c,0,extension), step=layerStep(c), normal=step.normalize(), delta=local.subtract(origin);
            double spacing=step.length();
            double along=delta.dotProduct(normal);
            if(delta.subtract(normal.multiply(along)).lengthSquared()>(radius+0.3)*(radius+0.3))return;
            long first=Math.max(0,(long)Math.floor((along-radius)/spacing)),last=Math.min(n,(long)Math.ceil((along+radius)/spacing)+1);
            for(long i=first;i<last;i++)result.add(new PufferAttachments.Mount(c.slot,(int)i,c));
        });
    }
    public static int[] counts(BoatEntity boat) {
        BoatAccess a=(BoatAccess)boat; int[] n=new int[FACES];
        n[BOTTOM]=PufferAttachments.count(a.longboat$bottomPuffers()); n[STERN]=PufferAttachments.sternCount(boat);
        a.longboat$gridPuffers().forEach((c,count)->n[c.face]+=count); return n;
    }
    public static boolean firing(BoatEntity boat,int face) {return (((BoatAccess)boat).longboat$visualState().getInt("JetFaces")&(1<<face))!=0;}
    /** Count-weighted local centers, computed per column rather than per fish. */
    public static Vec3d[] centers(BoatEntity boat) {
        Vec3d[] sums=new Vec3d[FACES]; Arrays.fill(sums,Vec3d.ZERO); BoatAccess a=(BoatAccess)boat;
        float ext=BoatGeometry.extension(boat);
        int stern=PufferAttachments.sternCount(boat);
        if(stern>0) sums[STERN]=new Vec3d(0,0.25+0.23*(stern-1)/8.0,-BoatGeometry.halfLength(boat)-0.095).multiply(stern);
        for(var e:a.longboat$bottomPuffers().entrySet()) {
            int n=e.getValue(); Vec3d center=new Vec3d(0,-0.14,(-a.longboat$segments()+1+e.getKey()*2.0)*ext).multiply(n);
            for(int i=0;i<n%15;i++)center=center.add(PufferAttachments.cell(i));
            sums[BOTTOM]=sums[BOTTOM].add(center);
        }
        a.longboat$gridPuffers().forEach((c,n)->sums[c.face]=sums[c.face].add(position(boat,c,0,ext).add(layerStep(c).multiply((n-1)/2.0)).multiply(n)));
        int[] counts=counts(boat); for(int f=0;f<FACES;f++) if(counts[f]>0)sums[f]=sums[f].multiply(1.0/counts[f]);
        return sums;
    }
}
