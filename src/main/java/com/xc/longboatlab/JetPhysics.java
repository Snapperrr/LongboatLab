package com.xc.longboatlab;

import net.minecraft.util.math.Vec3d;

/** Horizontal tuning is independent of gravity; all upward/downward nozzles share a lift budget. */
public final class JetPhysics {
    private JetPhysics() {}
    public static Vec3d[] accelerations(int[] counts,Vec3d[] axes,double mass,double multiplier) {
        Vec3d[] result=new Vec3d[PufferGrid.FACES];
        double up=0,down=0,upCount=0,downCount=0;
        for(int f=0;f<result.length;f++) {
            result[f]=Vec3d.ZERO;
            if(counts[f]<=0 || axes[f]==null)continue;
            double root=Math.sqrt(counts[f]);
            double horizontal=f==PufferGrid.BOTTOM?Math.min(0.075,0.018*root/Math.sqrt(mass))
                    :Math.min(0.45,(0.075+0.035*root)/Math.sqrt(mass));
            double vertical=Math.min(0.075,0.018*root/mass);
            Vec3d axis=axes[f];result[f]=new Vec3d(axis.x*horizontal,axis.y*vertical,axis.z*horizontal);
            if(axis.y>0) { up+=result[f].y;upCount+=counts[f]*axis.y*axis.y; }
            else { down-=result[f].y;downCount+=counts[f]*axis.y*axis.y; }
        }
        // sqrt(N1)+sqrt(N2) previously rewarded splitting a small supply across several faces.
        // An aligned single face is unchanged; summing projected counts avoids that extra lift.
        double upScale=up>0?Math.min(1,Math.min(0.075,0.018*Math.sqrt(upCount)/mass)/up):1;
        double downScale=down>0?Math.min(1,Math.min(0.075,0.018*Math.sqrt(downCount)/mass)/down):1;
        for(int f=0;f<result.length;f++) {
            Vec3d a=result[f];
            if(a==Vec3d.ZERO)continue;
            result[f]=new Vec3d(a.x,a.y*(a.y>0?upScale:downScale),a.z).multiply(multiplier);
        }
        return result;
    }
}
