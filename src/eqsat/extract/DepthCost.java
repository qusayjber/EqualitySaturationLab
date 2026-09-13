package eqsat.extract;

import eqsat.model.Op;

import java.util.List;

public final class DepthCost implements CostModel {
    @Override public String name() { return "Depth"; }
    @Override public String description() { return "Minimises expression depth (1 + max of children)."; }
    @Override public double nodeCost(Op op, String label) { return 1; }
    @Override public double combine(Op op, String label, List<Double> childCosts) {
        double max = 0;
        for (double c : childCosts) max = Math.max(max, c);
        return 1 + max;
    }
}