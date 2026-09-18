package com.xc.longboatlab.client;

import com.xc.longboatlab.JetSettingsPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientJetSettings {
    private static float density = 1, force = 1;
    private ClientJetSettings() {}
    public static float density() { return density; }
    public static float force() { return force; }
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(JetSettingsPayload.ID, (payload, context) -> {
            density = payload.density(); force = payload.force();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { density = force = 1; });
    }
}
