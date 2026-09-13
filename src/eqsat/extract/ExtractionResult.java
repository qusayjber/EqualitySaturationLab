package eqsat.extract;

import eqsat.model.Expr;

import java.util.Map;

public record ExtractionResult(Expr expression, double cost, Map<Integer, Double> classCosts) {
    public ExtractionResult { classCosts = Map.copyOf(classCosts); }
}