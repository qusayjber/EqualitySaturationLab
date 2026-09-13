package eqsat.extract;

import eqsat.model.Op;

public final class NodeCountCost implements CostModel {
    @Override public String name() { return "AST Node Count"; }
    @Override public String description() { return "Every node costs 1 — minimises total AST size."; }
    @Override public double nodeCost(Op op, String label) { return 1; }
}