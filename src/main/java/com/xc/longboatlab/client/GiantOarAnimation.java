package com.xc.longboatlab.client;

import com.xc.longboatlab.GiantOars;
import com.xc.longboatlab.LoadedBoats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import java.util.Map;
import java.util.WeakHashMap;

public final class GiantOarAnimation {
    private record Frame(double oldLeft, double left, double oldRight, double right) {}
    private static final Map<BoatEntity, Frame> FRAMES = new WeakHashMap<>();
    private GiantOarAnimation() {}
    public static void tick(MinecraftClient client) {
        if (client.world == null) { FRAMES.clear(); return; }
        for (BoatEntity boat : LoadedBoats.in(client.world)) {
            if (GiantOars.mounted(boat).isEmpty()) continue;
            double left = GiantOars.phase(boat, true), right = GiantOars.phase(boat, false);
            Frame old = FRAMES.get(boat);
            FRAMES.put(boat, new Frame(old == null ? left : old.left, left, old == null ? right : old.right, right));
        }
    }
    public static double phase(BoatEntity boat, boolean left, float delta) {
        Frame frame = FRAMES.get(boat);
        if (frame == null) return GiantOars.phase(boat, left);
        return left ? frame.oldLeft + (frame.left - frame.oldLeft) * delta : frame.oldRight + (frame.right - frame.oldRight) * delta;
    }
}
