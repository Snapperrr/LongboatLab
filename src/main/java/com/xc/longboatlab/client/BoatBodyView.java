package com.xc.longboatlab.client;

import com.xc.longboatlab.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

/** Interpolation only; the server's full rotations and collision solver own the attitude. */
public final class BoatBodyView {
    private BoatBodyView() {}
    public static void tick(MinecraftClient client) {
        if(client.world==null){BoatBody.clearVisuals();return;}
        for(BoatEntity boat:LoadedBoats.in(client.world))BoatBody.sampleVisual(boat);
    }
    public static double pitch(BoatEntity boat,float delta){return BoatBody.visualPitch(boat,delta);}
    public static double roll(BoatEntity boat,float delta){return BoatBody.visualRoll(boat,delta);}
    public static org.joml.Quaternionf riderRotation(BoatEntity boat,float delta) {
        float yaw=(float)Math.toRadians(BoatBody.visualYaw(boat,delta));
        return new org.joml.Quaternionf().rotationY(-yaw).rotateZ((float)roll(boat,delta)).rotateX((float)-pitch(boat,delta)).rotateY(yaw);
    }
    public static Vec3d offset(BoatEntity boat, Vec3d local, float yaw, float delta) {
        return BoatBody.visualOffset(boat,local,yaw,delta);
    }
}
