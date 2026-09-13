package eqsat.saturation;

import java.util.List;

public record SaturationStep(
        int iteration,
        int nodesBefore, int nodesAfter,
        int classesBefore, int classesAfter,
        int merges,
        List<RuleApplication> applications,
        long elapsedMillis,
        boolean changed) {

    public SaturationStep { applications = List.copyOf(applications); }

    public String summary() {
        if (!changed) return "Iteration " + iteration + " — fixpoint reached (no changes).";
        return "Iteration " + iteration + " — " + applications.size() + " rewrite application(s), "
                + merges + " merge(s), nodes " + nodesBefore + " → " + nodesAfter
                + ", classes " + classesBefore + " → " + classesAfter;
    }
}