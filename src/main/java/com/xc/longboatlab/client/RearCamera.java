package com.xc.longboatlab.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;

/** World-oriented rear view: follow hull translation, never rider look or hull rotation. */
public final class RearCamera {
    private static final float MIN_DISTANCE = 2.5f, MAX_DISTANCE = 18.0f;
    private static boolean enabled;
    private static float previousDistance = 7, distance = 7, targetDistance = 7, yaw;
    private static BoatEntity subject;
    private static Perspective previousPerspective = Perspective.FIRST_PERSON;
    private RearCamera() {}

    public static boolean active() {
        var client=MinecraftClient.getInstance();
        return enabled && client.player!=null && client.player.getVehicle()==subject
                && client.getCameraEntity()==client.player && client.options.getPerspective()==Perspective.THIRD_PERSON_BACK;
    }
    public static float yaw() { return yaw; }
    public static float distance(float delta) { return MathHelper.lerp(delta,previousDistance,distance); }
    private static void disable(MinecraftClient client, boolean restore) {
        if(enabled && restore && client.options.getPerspective()==Perspective.THIRD_PERSON_BACK)
            client.options.setPerspective(previousPerspective);
        enabled=false;subject=null;
    }
    public static void tick(MinecraftClient client) {
        if(client.player==null || client.world==null || client.player.getVehicle()!=subject) disable(client,true);
        else if(enabled && client.options.getPerspective()!=Perspective.THIRD_PERSON_BACK) disable(client,false);
        previousDistance=distance;
        distance+=(targetDistance-distance)*0.4f;
    }
    public static void toggle(MinecraftClient client) {
        if (!(client.player != null && client.player.getVehicle() instanceof BoatEntity)) {
            disable(client,true);
            return;
        }
        if(enabled) { disable(client,true); return; }
        subject=(BoatEntity)client.player.getVehicle();
        previousPerspective=client.options.getPerspective();
        yaw=client.player.getYaw();
        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        enabled=true;
        previousDistance=distance=targetDistance=MathHelper.clamp(distance,MIN_DISTANCE,MAX_DISTANCE);
        BoatHud.notice(net.minecraft.text.Text.translatable("message.longboatlab.rear_camera"));
    }
    public static void recenter(MinecraftClient client) {
        if(active()) yaw=client.player.getYaw();
        else toggle(client);
    }
    /** Returns true when the wheel was consumed by the camera instead of the hotbar. */
    public static boolean scroll(MinecraftClient client, double amount) {
        if (!active() || client.currentScreen != null || !client.isWindowFocused() || client.player == null
                || !(client.player.getVehicle() instanceof BoatEntity) || !Double.isFinite(amount)) return false;
        targetDistance = MathHelper.clamp(targetDistance - (float) amount * 0.85f, MIN_DISTANCE, MAX_DISTANCE);
        return true;
    }
}
