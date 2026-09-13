package eqsat.saturation;

public record SaturationResult(
        SaturationStatus status,
        int iterations,
        int nodes,
        int classes,
        int peakNodes,
        int peakClasses,
        long rewriteApplications,
        long rewriteMatches,
        long unionOperations,
        long congruenceMerges,
        long elapsedMillis,
        boolean saturated) {
}