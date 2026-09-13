package eqsat.extract;

import eqsat.model.Op;

import java.util.EnumMap;
import java.util.Map;

public final class WeightedCost implements CostModel {

    private final EnumMap<Op, Double> weights = new EnumMap<>(Op.class);

    public WeightedCost() {
        weights.put(Op.VAR, 1.0);
        weights.put(Op.CONST, 1.0);
        weights.put(Op.ADD, 1.0);
        weights.put(Op.SUB, 1.0);
        weights.put(Op.MUL, 2.0);
        weights.put(Op.DIV, 4.0);
        weights.put(Op.POW, 5.0);
        weights.put(Op.NEG, 1.0);
    }

    public void setWeight(Op op, double w) { weights.put(op, w); }
    public double weight(Op op) { return weights.getOrDefault(op, 1.0); }
    public Map<Op, Double> weights() { return weights; }

    @Override public String name() { return "Weighted Cost"; }
    @Override public String description() { return "Per-operator weights (ADD=1, MUL=2, DIV=4, POW=5 by default)."; }
    @Override public double nodeCost(Op op, String label) { return weight(op); }
}