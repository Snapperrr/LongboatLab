package com.xc.longboatlab;

public interface BoatAccess {
    java.util.List<GiantOars.Mounted> longboat$giants();
    int longboat$segments();
    int longboat$width();
    int longboat$oars(boolean left);
    BoatRig longboat$rig();
    void longboat$setRig(BoatRig rig);
    boolean longboat$compressed();
    int longboat$puffers();
    java.util.Map<Integer, Integer> longboat$bottomPuffers();
    java.util.Map<PufferGrid.Cell, Integer> longboat$gridPuffers();
    BoatAbilities longboat$abilities();
    net.minecraft.nbt.NbtCompound longboat$visualState();
    void longboat$syncAbilities();
    net.minecraft.nbt.NbtCompound longboat$seatData();
    void longboat$setSeatData(net.minecraft.nbt.NbtCompound seats);
}
