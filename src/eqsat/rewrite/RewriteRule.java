package eqsat.rewrite;

public interface RewriteRule {
    String name();
    String description();
    RuleCategory category();
    RuleSafety safety();
    Pattern left();
    Pattern right();

    default String patternText()      { return left().render(); }
    default String replacementText()  { return right().render(); }
    default String equationText()     { return patternText() + "  ⟶  " + replacementText(); }
}