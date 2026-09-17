package com.xc.longboatlab;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/** A clicked hull position survives boat interpolation; the server checks reach and occlusion. */
public record BoatBoardPayload(int entityId, boolean offHand, double x, double y, double z) implements CustomPayload {
    public static final Id<BoatBoardPayload> ID = new Id<>(Identifier.of(LongboatLab.ID, "board"));
    public static final PacketCodec<ByteBuf, BoatBoardPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BoatBoardPayload::entityId,
            PacketCodecs.BOOL, BoatBoardPayload::offHand,
            PacketCodecs.DOUBLE, BoatBoardPayload::x,
            PacketCodecs.DOUBLE, BoatBoardPayload::y,
            PacketCodecs.DOUBLE, BoatBoardPayload::z, BoatBoardPayload::new);
    public Vec3d localHit() { return new Vec3d(x,y,z); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
