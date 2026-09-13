package eqsat.rewrite;

public enum RuleSafety {
    /** Universally valid for all values. */
    SAFE("Safe", "Valid for every value of the pattern variables."),
    /** Valid only under conditions on the matched terms. */
    CONDITIONAL("Conditional", "Valid only under an extra precondition (e.g. x != 0)."),
    /** Valid only under domain assumptions (e.g. no overflow, real numbers). */
    ASSUMPTION("Assumption", "Valid only under domain assumptions about the operands.");

    private final String label;
    private final String explanation;

    RuleSafety(String label, String explanation) {
        this.label = label;
        this.explanation = explanation;
    }
    public String label() { return label; }
    public String explanation() { return explanation; }
}