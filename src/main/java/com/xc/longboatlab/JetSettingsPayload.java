package com.xc.longboatlab;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server-to-client only. Clients cannot change the world's propulsion settings. */
public record JetSettingsPayload(float density, float force) implements CustomPayload {
    public static final Id<JetSettingsPayload> ID = new Id<>(Identifier.of(LongboatLab.ID, "jet_settings"));
    public static final PacketCodec<RegistryByteBuf, JetSettingsPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, JetSettingsPayload::density, PacketCodecs.FLOAT, JetSettingsPayload::force,
            JetSettingsPayload::new);
    public JetSettingsPayload {
        density = JetSettings.bounded(density, JetSettings.MAX_DENSITY);
        force = JetSettings.bounded(force, JetSettings.MAX_FORCE);
    }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
