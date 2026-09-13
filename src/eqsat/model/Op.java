package eqsat.model;

/** Operators AND leaf kinds (VAR / CONST) so that E-Nodes stay uniform. */
public enum Op {
    VAR("", 0, 0),
    CONST("", 0, 0),
    ADD("+", 2, 1),
    SUB("-", 2, 1),
    MUL("*", 2, 2),
    DIV("/", 2, 2),
    POW("^", 2, 3),
    NEG("-", 1, 4);

    private final String symbol;
    private final int arity;
    private final int precedence;

    Op(String symbol, int arity, int precedence) {
        this.symbol = symbol;
        this.arity = arity;
        this.precedence = precedence;
    }

    public String symbol() { return symbol; }
    public int arity() { return arity; }
    public int precedence() { return precedence; }
    public boolean isLeaf() { return this == VAR || this == CONST; }
    public boolean isCommutative() { return this == ADD || this == MUL; }

    /** "ADD" -> "Add" */
    public String displayName() {
        String n = name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }
}