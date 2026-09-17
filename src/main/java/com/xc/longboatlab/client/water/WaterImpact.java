package com.xc.longboatlab.client.water;

import java.util.UUID;
import net.minecraft.util.math.Vec3d;

/** Immutable client contact event. Positions are world coordinates; speed is blocks/tick.
 * Velocity is hull motion relative to the water; waterFlow restores world-space spray motion.
 * Area is a sampled footprint, not displaced fluid volume or an authoritative force.
 */
public record WaterImpact(UUID boat, Vec3d position, Vec3d velocity, Vec3d surfaceNormal,
                          Vec3d hullNormal, double area, double normalSpeed, double tangentSpeed,
                          double strength, Kind kind, int wakeSide, Vec3d waterFlow) {
    public WaterImpact(UUID boat, Vec3d position, Vec3d velocity, Vec3d surfaceNormal,
                       Vec3d hullNormal, double area, double normalSpeed, double tangentSpeed,
                       double strength, Kind kind, int wakeSide) {
        this(boat, position, velocity, surfaceNormal, hullNormal, area, normalSpeed, tangentSpeed,
                strength, kind, wakeSide, Vec3d.ZERO);
    }
    /** Compatibility constructor for non-wake contacts and existing surface providers. */
    public WaterImpact(UUID boat, Vec3d position, Vec3d velocity, Vec3d surfaceNormal,
                       Vec3d hullNormal, double area, double normalSpeed, double tangentSpeed,
                       double strength, Kind kind) {
        this(boat, position, velocity, surfaceNormal, hullNormal, area, normalSpeed, tangentSpeed, strength, kind, 0, Vec3d.ZERO);
    }
    public enum Kind { ENTRY, SKIM }
}
