package eqsat.saturation;

public record SaturationLimits(int maxIterations, int maxNodes, int maxClasses, long maxMillis) {

    public static SaturationLimits defaults() {
        return new SaturationLimits(25, 5_000, 2_000, 15_000);
    }

    public static SaturationLimits of(int iterations, int nodes) {
        return new SaturationLimits(iterations, nodes, 5_000, 20_000);
    }
}