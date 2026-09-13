package eqsat.extract;

import eqsat.egraph.EClass;
import eqsat.egraph.EGraph;
import eqsat.egraph.ENode;
import eqsat.model.*;

import java.util.*;

/**
 * Bottom-up cost-based extraction over the E-Graph.
 * Implemented as a least-fixed-point iteration so that (harmlessly) cyclic
 * E-Graphs still terminate.
 */
public final class Extractor {

    private Extractor() {}

    public static ExtractionResult extract(EGraph g, int rootClass, CostModel cm) {
        List<EClass> classes = g.classes();
        Map<Integer, Double> cost = new HashMap<>();
        Map<Integer, ENode> best = new HashMap<>();

        boolean changed = true;
        int guard = 0;
        while (changed && guard++ < 512) {
            changed = false;
            for (EClass c : classes) {
                for (ENode n : c.nodes()) {
                    List<Double> childCosts = new ArrayList<>(n.children().size());
                    boolean ready = true;
                    for (int ch : n.children()) {
                        Double cc = cost.get(g.find(ch));
                        if (cc == null || cc.isInfinite()) { ready = false; break; }
                        childCosts.add(cc);
                    }
                    if (!ready) continue;

                    double total = cm.combine(n.op(), n.label(), childCosts);
                    Double current = cost.get(c.id());
                    if (current == null || total < current) {
                        cost.put(c.id(), total);
                        best.put(c.id(), n);
                        changed = true;
                    }
                }
            }
        }

        int root = g.find(rootClass);
        Double rootCost = cost.get(root);
        if (rootCost == null) {
            return new ExtractionResult(null, Double.POSITIVE_INFINITY, cost);
        }
        Expr expr = rebuild(g, root, best, new HashSet<>());
        return new ExtractionResult(expr, rootCost, cost);
    }

    private static Expr rebuild(EGraph g, int classId, Map<Integer, ENode> best, Set<Integer> onStack) {
        int c = g.find(classId);
        ENode n = best.get(c);
        if (n == null) throw new IllegalStateException("No best node for EClass #" + c);
        if (!onStack.add(c)) throw new IllegalStateException("Cyclic extraction at EClass #" + c);

        Expr result;
        switch (n.op()) {
            case VAR   -> result = new VarExpr(n.label());
            case CONST -> result = new ConstExpr(Long.parseLong(n.label()));
            case NEG   -> result = new UnaryExpr(Op.NEG, rebuild(g, n.children().get(0), best, onStack));
            default    -> result = new BinaryExpr(n.op(),
                    rebuild(g, n.children().get(0), best, onStack),
                    rebuild(g, n.children().get(1), best, onStack));
        }
        onStack.remove(c);
        return result;
    }
}