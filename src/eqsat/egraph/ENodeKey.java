package eqsat.egraph;

import eqsat.model.Op;

import java.util.List;

/** Canonical hash-cons key: op + label + *canonical* child class ids. */
public record ENodeKey(Op op, String label, List<Integer> children) {
    public ENodeKey {
        children = List.copyOf(children);
    }
}