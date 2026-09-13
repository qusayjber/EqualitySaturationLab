package eqsat.rewrite;

import eqsat.model.Op;

/** Matches a concrete leaf E-Node (a literal constant, or a named variable). */
public record PatLeaf(Op op, String label) implements Pattern {
    public PatLeaf {
        if (!op.isLeaf()) throw new IllegalArgumentException("PatLeaf requires VAR or CONST");
    }
    @Override public String render() {
        return op == Op.CONST ? label : label;
    }
    @Override public String toString() { return render(); }
}