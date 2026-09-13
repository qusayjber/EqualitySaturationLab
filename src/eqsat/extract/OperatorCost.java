package eqsat.extract;

import eqsat.model.Op;

public final class OperatorCost implements CostModel {
    @Override public String name() { return "Operator Count"; }
    @Override public String description() { return "Only operators cost 1; leaves are free."; }
    @Override public double nodeCost(Op op, String label) { return op.isLeaf() ? 0 : 1; }
}