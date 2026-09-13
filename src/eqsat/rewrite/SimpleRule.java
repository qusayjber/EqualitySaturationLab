package eqsat.rewrite;

public record SimpleRule(
        String name,
        String description,
        RuleCategory category,
        RuleSafety safety,
        Pattern left,
        Pattern right) implements RewriteRule {

    @Override public String toString() { return name; }
}