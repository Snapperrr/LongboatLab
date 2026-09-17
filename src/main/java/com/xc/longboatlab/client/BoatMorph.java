package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatBody;
import net.minecraft.entity.vehicle.BoatEntity;

/** Morph, body attitude and rider positions share one sampled render frame. */
public final class BoatMorph {
    private BoatMorph() {}
    public static float extension(BoatEntity boat,float delta) {return BoatBody.visualExtension(boat,delta);}
}
