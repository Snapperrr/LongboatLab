package com.xc.longboatlab;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Only key states cross the network. The server chooses forces, range and anchor positions. */
public record BoatControlPayload(int held, int actions) implements CustomPayload {
    public static final int FORWARD = 1, BACK = 2, LEFT = 4, RIGHT = 8;
    public static final int UP = 16, DOWN = 32, ORBIT_LEFT = 64, ORBIT_RIGHT = 128;
    public static final int JET_HELD = 256;
    public static final int JET_BOTTOM=512, JET_STERN=1024, JET_BOW=2048, JET_LEFT=4096, JET_RIGHT=8192;
    public static final int TOGGLE = 1, HOOK = 2, JUMP = 4;
    public static final Id<BoatControlPayload> ID = new Id<>(Identifier.of(LongboatLab.ID, "controls"));
    public static final PacketCodec<ByteBuf, BoatControlPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, BoatControlPayload::held, PacketCodecs.VAR_INT, BoatControlPayload::actions,
            BoatControlPayload::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
