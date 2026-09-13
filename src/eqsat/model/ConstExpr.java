package eqsat.model;

public record ConstExpr(long value) implements Expr {
    @Override public String pretty() { return Long.toString(value); }
    @Override public int precedence() { return Integer.MAX_VALUE; }
    @Override public String toString() { return pretty(); }
}