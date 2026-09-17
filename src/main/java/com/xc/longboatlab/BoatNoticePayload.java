package com.xc.longboatlab;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record BoatNoticePayload(Text message) implements CustomPayload {
    public static final Id<BoatNoticePayload> ID = new Id<>(Identifier.of(LongboatLab.ID, "hud_notice"));
    public static final PacketCodec<RegistryByteBuf, BoatNoticePayload> CODEC = PacketCodec.tuple(
            TextCodecs.REGISTRY_PACKET_CODEC, BoatNoticePayload::message, BoatNoticePayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
    public static void send(PlayerEntity player, Text message) {
        if (player instanceof ServerPlayerEntity server && ServerPlayNetworking.canSend(server, ID)) {
            ServerPlayNetworking.send(server, new BoatNoticePayload(message));
        } else player.sendMessage(message, false);
    }
}
