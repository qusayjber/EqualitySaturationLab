package eqsat.model;

public sealed interface Expr permits VarExpr, ConstExpr, UnaryExpr, BinaryExpr {

    /** Fully-parenthesised-where-necessary rendering. */
    String pretty();

    /** Binding strength of the top node (higher = tighter). */
    int precedence();

    default boolean structuralEquals(Expr other) { return this.equals(other); }

    default String kind() { return getClass().getSimpleName(); }
}