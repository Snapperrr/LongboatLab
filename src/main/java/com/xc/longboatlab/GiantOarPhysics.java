package com.xc.longboatlab;

/** Size-dependent ground drive. Distances are blocks and time is server ticks. */
public final class GiantOarPhysics {
    private GiantOarPhysics() {}
    public static double mass(int length, int width, int riders) {
        return 1 + (length * (double) width - 1) * 0.30 + riders * 0.45;
    }
    public static double speed(double area, double mass) {
        double effort = Math.sqrt(area / mass);
        return 0.16 + 1.65 * effort / (effort + 4);
    }
    public static double acceleration(double area, double mass) {
        double effort = area / mass;
        return 0.045 + 0.55 * effort / (effort + 8);
    }
    public static double stepHeight(double size, double area, double mass) {
        double support = area / (area + mass * 0.4);
        // Reach grows with blade size; only supported, collision-checked steps use
        // it. Work remains bounded for command-created paddle dimensions.
        return Math.min(8, (0.30 + size * 0.60) * support);
    }
    public static double jointLift(double size, double area, double mass) {
        return Math.min(0.40, 0.08 + 0.06 * size) * area / (area + mass * 0.4);
    }
}
