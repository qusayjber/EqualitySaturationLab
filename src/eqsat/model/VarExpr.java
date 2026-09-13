package eqsat.model;

public record VarExpr(String name) implements Expr {
    public VarExpr {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("variable name must not be blank");
    }
    @Override public String pretty() { return name; }
    @Override public int precedence() { return Integer.MAX_VALUE; }
    @Override public String toString() { return pretty(); }
}