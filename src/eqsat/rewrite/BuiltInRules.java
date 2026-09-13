package eqsat.rewrite;

import eqsat.model.Op;

import java.util.ArrayList;
import java.util.List;

public final class BuiltInRules {

    private BuiltInRules() {}

    // ---------- pattern DSL ----------
    private static PatVar v(String n)              { return new PatVar(n); }
    private static PatLeaf c(long value)           { return new PatLeaf(Op.CONST, Long.toString(value)); }
    private static PatNode n(Op op, Pattern... ks) { return new PatNode(op, List.of(ks)); }

    private static RewriteRule rule(String name, RuleCategory cat, RuleSafety safety,
                                    String desc, Pattern l, Pattern r) {
        return new SimpleRule(name, desc, cat, safety, l, r);
    }

    // ---------- library ----------
    public static List<RewriteRule> defaultRules() {
        List<RewriteRule> r = new ArrayList<>();

        // ---- Identity / annihilator ----
        r.add(rule("AddZeroRight", RuleCategory.IDENTITY, RuleSafety.SAFE,
                "Adding zero on the right is a no-op.", n(Op.ADD, v("x"), c(0)), v("x")));
        r.add(rule("AddZeroLeft", RuleCategory.IDENTITY, RuleSafety.SAFE,
                "Adding zero on the left is a no-op.", n(Op.ADD, c(0), v("x")), v("x")));
        r.add(rule("MulOneRight", RuleCategory.IDENTITY, RuleSafety.SAFE,
                "Multiplying by one on the right is a no-op.", n(Op.MUL, v("x"), c(1)), v("x")));
        r.add(rule("MulOneLeft", RuleCategory.IDENTITY, RuleSafety.SAFE,
                "Multiplying by one on the left is a no-op.", n(Op.MUL, c(1), v("x")), v("x")));
        r.add(rule("MulZeroRight", RuleCategory.ARITHMETIC, RuleSafety.SAFE,
                "Multiplying by zero annihilates the expression.", n(Op.MUL, v("x"), c(0)), c(0)));
        r.add(rule("MulZeroLeft", RuleCategory.ARITHMETIC, RuleSafety.SAFE,
                "Zero times anything is zero.", n(Op.MUL, c(0), v("x")), c(0)));
        r.add(rule("SubZero", RuleCategory.SIMPLIFICATION, RuleSafety.SAFE,
                "Subtracting zero is a no-op.", n(Op.SUB, v("x"), c(0)), v("x")));
        r.add(rule("DivOne", RuleCategory.SIMPLIFICATION, RuleSafety.SAFE,
                "Dividing by one is a no-op.", n(Op.DIV, v("x"), c(1)), v("x")));

        // ---- Double negation ----
        r.add(rule("DoubleNegation", RuleCategory.SIMPLIFICATION, RuleSafety.SAFE,
                "Negating twice restores the original value.",
                n(Op.NEG, n(Op.NEG, v("x"))), v("x")));

        // ---- Associativity ----
        r.add(rule("AddAssociativityRight", RuleCategory.ASSOCIATIVITY, RuleSafety.SAFE,
                "Regroup an addition to the right.",
                n(Op.ADD, n(Op.ADD, v("x"), v("y")), v("z")),
                n(Op.ADD, v("x"), n(Op.ADD, v("y"), v("z")))));
        r.add(rule("AddAssociativityLeft", RuleCategory.ASSOCIATIVITY, RuleSafety.SAFE,
                "Regroup an addition to the left.",
                n(Op.ADD, v("x"), n(Op.ADD, v("y"), v("z"))),
                n(Op.ADD, n(Op.ADD, v("x"), v("y")), v("z"))));
        r.add(rule("MulAssociativityRight", RuleCategory.ASSOCIATIVITY, RuleSafety.SAFE,
                "Regroup a multiplication to the right.",
                n(Op.MUL, n(Op.MUL, v("x"), v("y")), v("z")),
                n(Op.MUL, v("x"), n(Op.MUL, v("y"), v("z")))));
        r.add(rule("MulAssociativityLeft", RuleCategory.ASSOCIATIVITY, RuleSafety.SAFE,
                "Regroup a multiplication to the left.",
                n(Op.MUL, v("x"), n(Op.MUL, v("y"), v("z"))),
                n(Op.MUL, n(Op.MUL, v("x"), v("y")), v("z"))));

        // ---- Commutativity ----
        r.add(rule("AddCommutativity", RuleCategory.COMMUTATIVITY, RuleSafety.SAFE,
                "Addition is commutative.", n(Op.ADD, v("x"), v("y")), n(Op.ADD, v("y"), v("x"))));
        r.add(rule("MulCommutativity", RuleCategory.COMMUTATIVITY, RuleSafety.SAFE,
                "Multiplication is commutative.", n(Op.MUL, v("x"), v("y")), n(Op.MUL, v("y"), v("x"))));

        // ---- Distributivity / factorisation ----
        r.add(rule("Distributivity", RuleCategory.DISTRIBUTIVITY, RuleSafety.SAFE,
                "Distribute a multiplication over an addition.",
                n(Op.MUL, v("x"), n(Op.ADD, v("y"), v("z"))),
                n(Op.ADD, n(Op.MUL, v("x"), v("y")), n(Op.MUL, v("x"), v("z")))));
        r.add(rule("Factorization", RuleCategory.DISTRIBUTIVITY, RuleSafety.SAFE,
                "Factor a common multiplicand out of a sum.",
                n(Op.ADD, n(Op.MUL, v("x"), v("y")), n(Op.MUL, v("x"), v("z"))),
                n(Op.MUL, v("x"), n(Op.ADD, v("y"), v("z")))));

        // ---- Arithmetic simplification ----
        r.add(rule("SubSelf", RuleCategory.SIMPLIFICATION, RuleSafety.SAFE,
                "Anything minus itself is zero.", n(Op.SUB, v("x"), v("x")), c(0)));
        r.add(rule("PowOne", RuleCategory.SIMPLIFICATION, RuleSafety.SAFE,
                "Anything to the power 1 is itself.", n(Op.POW, v("x"), c(1)), v("x")));
        r.add(rule("PowZero", RuleCategory.SIMPLIFICATION, RuleSafety.SAFE,
                "Anything to the power 0 is 1.", n(Op.POW, v("x"), c(0)), c(1)));
        r.add(rule("SubToAddNeg", RuleCategory.ARITHMETIC, RuleSafety.SAFE,
                "Subtraction is addition of the negation.",
                n(Op.SUB, v("x"), v("y")), n(Op.ADD, v("x"), n(Op.NEG, v("y")))));
        r.add(rule("DivSelf", RuleCategory.SIMPLIFICATION, RuleSafety.CONDITIONAL,
                "x / x = 1, valid only when x != 0.",
                n(Op.DIV, v("x"), v("x")), c(1)));

        // ---- Strength reduction ----
        r.add(rule("MulTwoToAdd", RuleCategory.STRENGTH_REDUCTION, RuleSafety.SAFE,
                "x * 2 can be computed as x + x.",
                n(Op.MUL, v("x"), c(2)), n(Op.ADD, v("x"), v("x"))));
        r.add(rule("AddSelfToMulTwo", RuleCategory.STRENGTH_REDUCTION, RuleSafety.SAFE,
                "x + x can be computed as x * 2.",
                n(Op.ADD, v("x"), v("x")), n(Op.MUL, v("x"), c(2))));
        r.add(rule("PowTwoToMul", RuleCategory.STRENGTH_REDUCTION, RuleSafety.SAFE,
                "x ^ 2 can be computed as x * x.",
                n(Op.POW, v("x"), c(2)), n(Op.MUL, v("x"), v("x"))));
        r.add(rule("MulTwoToPow", RuleCategory.STRENGTH_REDUCTION, RuleSafety.SAFE,
                "x * x can be computed as x ^ 2.",
                n(Op.MUL, v("x"), v("x")), n(Op.POW, v("x"), c(2))));

        // ---- Additional algebra ----
        r.add(rule("AddZeroConst", RuleCategory.ALGEBRA, RuleSafety.SAFE,
                "0 + 0 collapses to 0.", n(Op.ADD, c(0), c(0)), c(0)));
        r.add(rule("MulOneConst", RuleCategory.ALGEBRA, RuleSafety.SAFE,
                "1 * 1 collapses to 1.", n(Op.MUL, c(1), c(1)), c(1)));

        return r;
    }
}