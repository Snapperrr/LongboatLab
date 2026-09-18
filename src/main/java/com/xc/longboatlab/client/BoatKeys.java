package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatControlPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.BoatEntity;
import org.lwjgl.glfw.GLFW;

import static com.xc.longboatlab.BoatControlPayload.*;

public final class BoatKeys {
    private static KeyBinding mode, hook, boost, up, down, left, right, hud, hudScale, waterGraph, rearCamera;
    private static final KeyBinding[] jets=new KeyBinding[5];
    private static boolean jumpWasDown;
    private static float fov, previousFov;
    private BoatKeys() {}

    private static KeyBinding key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.longboatlab." + name,
                InputUtil.Type.KEYSYM, code, "category.longboatlab"));
    }

    public static void register() {
        mode = key("mode", GLFW.GLFW_KEY_R);
        hud = key("hud", GLFW.GLFW_KEY_H);
        hudScale = key("hud_scale", GLFW.GLFW_KEY_J);
        waterGraph = key("water_graph", GLFW.GLFW_KEY_F8);
        rearCamera = key("rear_camera", GLFW.GLFW_KEY_F7);
        hook = key("hook", GLFW.GLFW_KEY_G);
        boost = key("boost", GLFW.GLFW_KEY_LEFT_ALT);
        jets[0]=key("jet_bottom",GLFW.GLFW_KEY_KP_5); jets[1]=key("jet_stern",GLFW.GLFW_KEY_KP_2);
        jets[2]=key("jet_bow",GLFW.GLFW_KEY_KP_8); jets[3]=key("jet_left",GLFW.GLFW_KEY_KP_4); jets[4]=key("jet_right",GLFW.GLFW_KEY_KP_6);
        up = key("orbit_up", GLFW.GLFW_KEY_UP);
        down = key("orbit_down", GLFW.GLFW_KEY_DOWN);
        left = key("orbit_left", GLFW.GLFW_KEY_LEFT);
        right = key("orbit_right", GLFW.GLFW_KEY_RIGHT);
        ClientTickEvents.END_CLIENT_TICK.register(BoatKeys::tick);
    }

    private static boolean take(KeyBinding key) {
        boolean pressed = false;
        while (key.wasPressed()) pressed = true;
        return pressed;
    }

    private static void tick(MinecraftClient client) {
        BoatItemPreview.worldChanged(client.world);
        BoatBodyView.tick(client);
        com.xc.longboatlab.client.water.BoatWaterEffects.tick(client);
        BoatExhaust.tick(client);
        GiantOarAnimation.tick(client);
        CableRenderer.tick(client);
        RearCamera.tick(client);
        if (take(hud) && client.currentScreen == null) BoatHud.toggle();
        if (take(hudScale) && client.currentScreen == null) BoatHud.cycleScale();
        if (take(waterGraph) && client.currentScreen == null && client.world != null)
            client.setScreen(new com.xc.longboatlab.client.water.WaterGraphScreen());
        if (take(rearCamera) && client.currentScreen == null) {
            if(net.minecraft.client.gui.screen.Screen.hasShiftDown()) RearCamera.recenter(client);
            else RearCamera.toggle(client);
        }
        take(boost);
        for(var jet:jets)take(jet);
        int actions = (take(mode) ? TOGGLE : 0) | (take(hook) ? HOOK : 0);
        boolean jump = client.options.jumpKey.isPressed();
        if (jump && !jumpWasDown) actions |= JUMP;
        jumpWasDown = jump;
        previousFov = fov;
        boolean riding = client.player != null && client.player.getVehicle() instanceof BoatEntity;
        float target = 0;
        if (riding) {
            BoatAccess access = (BoatAccess) client.player.getVehicle();
            if (access.longboat$visualState().getInt("Boost") > 0) target = Math.min(0.45f, 0.12f + access.longboat$puffers() * 0.018f);
        }
        fov += (target - fov) * (target > fov ? 0.4f : 0.16f);
        if (!riding || !ClientPlayNetworking.canSend(BoatControlPayload.ID)
                || ((BoatEntity) client.player.getVehicle()).getControllingPassenger() != client.player) return;
        int held = 0;
        if (client.currentScreen == null && client.isWindowFocused()) {
            if (boost.isPressed()) held |= JET_HELD;
            for(int f=0;f<5;f++)if(jets[f].isPressed())held|=JET_BOTTOM<<f;
            if (client.options.forwardKey.isPressed()) held |= FORWARD;
            if (client.options.backKey.isPressed()) held |= BACK;
            if (client.options.leftKey.isPressed()) held |= LEFT;
            if (client.options.rightKey.isPressed()) held |= RIGHT;
            if (up.isPressed()) held |= UP;
            if (down.isPressed()) held |= DOWN;
            if (left.isPressed()) held |= ORBIT_LEFT;
            if (right.isPressed()) held |= ORBIT_RIGHT;
        } else actions = 0;
        ClientPlayNetworking.send(new BoatControlPayload(held, actions));
    }

    public static double fovBoost(float tickDelta) {
        if(RearCamera.active())return 0;
        MinecraftClient client = MinecraftClient.getInstance();
        return (previousFov + (fov - previousFov) * tickDelta) * client.options.getFovEffectScale().getValue();
    }
}
