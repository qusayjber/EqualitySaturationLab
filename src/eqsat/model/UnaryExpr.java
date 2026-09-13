package eqsat.model;

import java.util.Objects;

public record UnaryExpr(Op op, Expr operand) implements Expr {
    public UnaryExpr {
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(operand, "operand");
        if (op.arity() != 1) throw new IllegalArgumentException("not a unary operator: " + op);
    }
    @Override public String pretty() {
        String s = operand.pretty();
        if (operand.precedence() < op.precedence()) s = "(" + s + ")";
        return op.symbol() + s;
    }
    @Override public int precedence() { return op.precedence(); }
    @Override public String toString() { return pretty(); }
}