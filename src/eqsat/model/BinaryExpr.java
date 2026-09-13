package eqsat.model;

import java.util.Objects;

public record BinaryExpr(Op op, Expr left, Expr right) implements Expr {
    public BinaryExpr {
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        if (op.arity() != 2) throw new IllegalArgumentException("not a binary operator: " + op);
    }

    @Override public String pretty() {
        int p = op.precedence();
        String l = left.pretty();
        String r = right.pretty();

        boolean leftParens = left.precedence() < p
                || (left.precedence() == p && op == Op.POW);            // ^ is right-assoc
        boolean rightParens = right.precedence() < p
                || (right.precedence() == p && (op == Op.SUB || op == Op.DIV));

        if (leftParens) l = "(" + l + ")";
        if (rightParens) r = "(" + r + ")";
        return l + " " + op.symbol() + " " + r;
    }

    @Override public int precedence() { return op.precedence(); }
    @Override public String toString() { return pretty(); }
}