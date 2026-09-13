package eqsat.extract;

import eqsat.model.Op;

import java.util.List;

public interface CostModel {

    String name();

    /** Cost of a single node, excluding children. */
    double nodeCost(Op op, String label);

    /** How child costs combine at a node. Default: node + sum(children). */
    default double combine(Op op, String label, List<Double> childCosts) {
        double total = nodeCost(op, label);
        for (double c : childCosts) total += c;
        return total;
    }

    default String description() { return name(); }
}