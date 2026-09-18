package com.xc.longboatlab.client.water;

/** Visual shallow-water shoulder and spilling crest, in blocks and ticks.
 * The shared height field remains responsible for the surrounding water surface.
 */
final class WakeProfile {
    static final int SAMPLES = 12;
    private WakeProfile() {}

    private static double smooth(double value) {
        double t = Math.max(0, Math.min(1, value));
        return t * t * (3 - 2 * t);
    }

    static double pressure(double behindBow, double outwardSpeed) {
        // The bow displaces water; a straight midship does not keep lifting a tall wall.
        // A turning hull can still push water out anywhere along its actual length.
        double bow = 0.24 + 0.76 * Math.exp(-Math.max(0, behindBow) / 2.6);
        return Math.max(bow, smooth(Math.max(0, outwardSpeed) / 0.55));
    }

    static double amplitude(double strength, double entry, double pressure, double width) {
        return Math.max(0, Math.min(width * 0.34,
                Math.min(0.54, (0.11 + strength * 0.48) * entry * pressure)));
    }

    static double breaking(double height, double width, double strength) {
        double steepness = height / Math.max(0.02, width);
        return smooth((steepness - 0.08) / 0.18) * (0.35 + 0.65 * smooth(strength / 0.90));
    }

    static double crest(double phase) {
        // Wavelengths exceed the hull sample spacing. Fine foam is in the texture, not
        // in rapidly moving vertices that alias when a long boat turns.
        double p = phase * Math.PI * 2;
        return 0.62 + 0.022 * Math.sin(p * 0.11 + 0.8) + 0.010 * Math.sin(p * 0.23);
    }

    static double height(double across, double crest) {
        double q = across <= crest ? across / crest : (1 - across) / (1 - crest);
        double shoulder = smooth(q);
        // Both feet and the peak have zero slope: no upright outer edge or pointed lip.
        return shoulder * shoulder;
    }

    static double modulation(double phase) {
        double p = phase * Math.PI * 2;
        return 1 + 0.045 * Math.sin(p * 0.11) + 0.020 * Math.sin(p * 0.23 + 1.2);
    }
}
