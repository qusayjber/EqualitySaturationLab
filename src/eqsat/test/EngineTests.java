package eqsat.test;

import eqsat.egraph.EGraph;
import eqsat.egraph.ENode;
import eqsat.extract.*;
import eqsat.model.*;
import eqsat.parser.Parser;
import eqsat.rewrite.*;
import eqsat.saturation.*;

import java.util.*;

public final class EngineTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        parserTests();
        astTests();
        egraphTests();
        unionFindTests();
        congruenceTests();
        patternTests();
        ruleTests();
        saturationTests();
        extractionTests();
        costModelTests();

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    // ------------------------------------------------------------ helpers

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  ok   " + name); }
        else    { failed++; System.out.println("  FAIL " + name); }
    }

    private static void eq(String name, Object a, Object b) {
        check(name + "  [" + a + " == " + b + "]", Objects.equals(a, b));
    }

    // ------------------------------------------------------------ 1-4 parser

    private static void parserTests() {
        System.out.println("Parser");
        eq("precedence", Parser.parse("a + b * c").pretty(), "a + b * c");
        eq("parens override", Parser.parse("(a + b) * c").pretty(), "(a + b) * c");
        eq("right assoc pow", Parser.parse("a ^ b ^ c").pretty(), "a ^ b ^ c");
        eq("unary minus", Parser.parse("-x + y").pretty(), "-x + y");
        eq("nested", Parser.parse("((a+b)+(c+d))").pretty(), "(a + b) + (c + d)");
        eq("division chain", Parser.parse("a / b / c").pretty(), "a / b / c");
        eq("constants", Parser.parse("10 + 100").pretty(), "10 + 100");

        boolean threw = false;
        try { Parser.parse("(a + b"); } catch (RuntimeException e) { threw = true; }
        check("unbalanced parens rejected", threw);

        threw = false;
        try { Parser.parse("a @ b"); } catch (RuntimeException e) { threw = true; }
        check("unknown token rejected", threw);
    }

    // ------------------------------------------------------------ 5-6 AST

    private static void astTests() {
        System.out.println("AST");
        Expr a = new VarExpr("a");
        Expr b = new VarExpr("b");
        Expr plus = new BinaryExpr(Op.ADD, a, b);
        eq("structural equality", plus, new BinaryExpr(Op.ADD, new VarExpr("a"), new VarExpr("b")));
        eq("size", Exprs.size(plus), 3);
        eq("depth", Exprs.depth(plus), 2);
    }

    // ------------------------------------------------------------ 7-10 E-Graph

    private static void egraphTests() {
        System.out.println("E-Graph");
        EGraph g = new EGraph();
        int a = g.add(ENode.var("a"));
        int b = g.add(ENode.var("b"));
        int add = g.add(ENode.of(Op.ADD, a, b));

        eq("3 classes", g.classCount(), 3);
        eq("3 nodes", g.nodeCount(), 3);

        int addAgain = g.add(ENode.of(Op.ADD, a, b));
        eq("hash-consing reuses the class", addAgain, add);
        eq("node count unchanged", g.nodeCount(), 3);

        // commutativity is NOT automatic — it is a rewrite
        int addComm = g.add(ENode.of(Op.ADD, b, a));
        check("commutative variant is a distinct class until rewritten", addComm != add);
    }

    // ------------------------------------------------------------ 11-12 union-find

    private static void unionFindTests() {
        System.out.println("Union-Find");
        eqsat.egraph.UnionFind uf = new eqsat.egraph.UnionFind();
        int x = uf.makeSet(), y = uf.makeSet(), z = uf.makeSet();
        uf.union(x, y);
        eq("x ~ y", uf.find(x), uf.find(y));
        check("z distinct", uf.find(z) != uf.find(x));
        uf.union(y, z);
        eq("transitive", uf.find(x), uf.find(z));
        eq("class count", uf.classCount(), 1);
    }

    // ------------------------------------------------------------ 13 congruence

    private static void congruenceTests() {
        System.out.println("Congruence closure");
        EGraph g = new EGraph();
        int a = g.add(ENode.var("a"));
        int b = g.add(ENode.var("b"));
        int c = g.add(ENode.var("c"));
        int addAC = g.add(ENode.of(Op.ADD, a, c));
        int addBC = g.add(ENode.of(Op.ADD, b, c));
        check("initially distinct", g.find(addAC) != g.find(addBC));

        g.union(a, b);
        g.rebuild();
        eq("f(a)==f(b) discovered by rebuild", g.find(addAC), g.find(addBC));
    }

    // ------------------------------------------------------------ 14-16 patterns

    private static void patternTests() {
        System.out.println("Pattern matching");
        EGraph g = new EGraph();
        Expr e = Parser.parse("(a + b) + 0");
        int root = g.addExpr(e);
        g.rebuild();

        Pattern addZero = new PatNode(Op.ADD, List.of(new PatVar("x"), new PatLeaf(Op.CONST, "0")));
        List<Map<String, Integer>> matches = Matcher.match(addZero, root, g);
        check("Add(?x, 0) matches (a+b)+0", !matches.isEmpty());

        Pattern addAny = new PatNode(Op.ADD, List.of(new PatVar("x"), new PatVar("y")));
        check("Add(?x, ?y) matches", !Matcher.match(addAny, root, g).isEmpty());

        Pattern noMatch = new PatNode(Op.MUL, List.of(new PatVar("x"), new PatVar("y")));
        check("Mul(?x, ?y) does not match", Matcher.match(noMatch, root, g).isEmpty());
    }

    // ------------------------------------------------------------ 17-20 rules

    private static void ruleTests() {
        System.out.println("Rewrite rules");
        List<RewriteRule> rules = BuiltInRules.defaultRules();
        check("library has >= 20 rules", rules.size() >= 20);
        check("rule names unique",
                rules.stream().map(RewriteRule::name).distinct().count() == rules.size());
        check("DivSelf marked conditional",
                rules.stream().anyMatch(r -> r.name().equals("DivSelf") && r.safety() == RuleSafety.CONDITIONAL));
        check("AddCommutativity present",
                rules.stream().anyMatch(r -> r.name().equals("AddCommutativity")));
    }

    // ------------------------------------------------------------ 21-24 saturation

    private static void saturationTests() {
        System.out.println("Saturation");

        // a + 0  →  a
        EGraph g1 = new EGraph();
        int root1 = g1.addExpr(Parser.parse("a + 0"));
        g1.rebuild();
        List<RewriteRule> onlyIdentity = BuiltInRules.defaultRules().stream()
                .filter(r -> r.name().equals("AddZeroRight")).toList();
        SaturationEngine e1 = new SaturationEngine(g1, onlyIdentity, SaturationLimits.of(10, 1000));
        e1.runToCompletion();
        ExtractionResult r1 = Extractor.extract(g1, root1, new NodeCountCost());
        eq("a + 0  →  a", r1.expression().pretty(), "a");
        eq("cost of a is 1", r1.cost(), 1.0);

        // (a+b)+c  with assoc + comm
        EGraph g2 = new EGraph();
        int root2 = g2.addExpr(Parser.parse("(a + b) + c"));
        g2.rebuild();
        List<RewriteRule> ac = BuiltInRules.defaultRules().stream()
                .filter(r -> r.name().equals("AddAssociativityRight")
                          || r.name().equals("AddAssociativityLeft")
                          || r.name().equals("AddCommutativity")).toList();
        SaturationEngine e2 = new SaturationEngine(g2, ac, SaturationLimits.of(20, 5000));
        e2.runToCompletion();

        Set<String> forms = new HashSet<>();
        for (Expr x : ExpressionEnumerator.enumerate(g2, root2, 200, 6)) forms.add(x.pretty());
        check("derives a + (b + c)", forms.contains("a + (b + c)"));
        check("derives (a + b) + c", forms.contains("(a + b) + c"));
        check("derives c + (a + b)", forms.contains("c + (a + b)"));
        check("E-Graph grew", g2.classCount() > 4);

        // saturation stops
        check("engine reports SATURATED", e2.status() == SaturationStatus.SATURATED);

        // x * 0  →  0
        EGraph g3 = new EGraph();
        int root3 = g3.addExpr(Parser.parse("x * 0"));
        g3.rebuild();
        SaturationEngine e3 = new SaturationEngine(g3,
                BuiltInRules.defaultRules().stream().filter(r -> r.name().equals("MulZeroRight")).toList(),
                SaturationLimits.of(5, 500));
        e3.runToCompletion();
        eq("x * 0  →  0", Extractor.extract(g3, root3, new NodeCountCost()).expression().pretty(), "0");
    }

    // ------------------------------------------------------------ 25-27 extraction

    private static void extractionTests() {
        System.out.println("Extraction");
        EGraph g = new EGraph();
        int root = g.addExpr(Parser.parse("(x + 0) * 1"));
        g.rebuild();
        SaturationEngine engine = new SaturationEngine(g, BuiltInRules.defaultRules(), SaturationLimits.of(15, 3000));
        engine.runToCompletion();

        Extractor.extract(g, root, new NodeCountCost());
        ExtractionResult res = Extractor.extract(g, root, new NodeCountCost());
        eq("(x + 0) * 1  →  x", res.expression().pretty(), "x");

        double originalCost = CostCalculator.costOf(Parser.parse("(x + 0) * 1"), new NodeCountCost());
        eq("original cost", originalCost, 5.0);
        check("optimised cost lower", res.cost() < originalCost);
    }

    // ------------------------------------------------------------ 28-30 cost models

    private static void costModelTests() {
        System.out.println("Cost models");
        Expr e = Parser.parse("a + b");
        eq("node count", CostCalculator.costOf(e, new NodeCountCost()), 3.0);
        eq("operator count", CostCalculator.costOf(e, new OperatorCost()), 1.0);

        WeightedCost w = new WeightedCost();
        eq("weighted ADD=1 (+2 leaves)", CostCalculator.costOf(e, w), 3.0);

        Expr m = Parser.parse("a * b");
        eq("weighted MUL=2 (+2 leaves)", CostCalculator.costOf(m, w), 4.0);

        eq("depth of a + b", CostCalculator.costOf(e, new DepthCost()), 2.0);
        eq("depth of (a+b)+c", CostCalculator.costOf(Parser.parse("(a + b) + c"), new DepthCost()), 3.0);
    }
}