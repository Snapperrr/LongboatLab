package com.xc.longboatlab;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OarInteractionPayload(int entityId, boolean offHand) implements CustomPayload {
    public static final Id<OarInteractionPayload> ID = new Id<>(Identifier.of(LongboatLab.ID, "oar_interaction"));
    public static final PacketCodec<ByteBuf, OarInteractionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, OarInteractionPayload::entityId,
            PacketCodecs.BOOL, OarInteractionPayload::offHand, OarInteractionPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
