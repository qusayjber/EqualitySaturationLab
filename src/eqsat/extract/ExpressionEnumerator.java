package eqsat.extract;

import eqsat.egraph.EClass;
import eqsat.egraph.EGraph;
import eqsat.egraph.ENode;
import eqsat.model.*;

import java.util.*;

/**
 * Bounded enumeration of the (possibly infinite) set of expressions
 * represented by an E-Class. Used by the equivalence view and cost explorer.
 */
public final class ExpressionEnumerator {

    private ExpressionEnumerator() {}

    public static List<Expr> enumerate(EGraph g, int rootClass, int maxResults, int maxDepth) {
        LinkedHashSet<Expr> out = new LinkedHashSet<>();
        expand(g, g.find(rootClass), 0, maxDepth, maxResults, new HashSet<>(), out);
        return new ArrayList<>(out);
    }

    private static void expand(EGraph g, int classId, int depth, int maxDepth,
                               int maxResults, Set<Integer> onStack, Set<Expr> out) {
        if (out.size() >= maxResults || depth > maxDepth) return;
        int c = g.find(classId);
        if (!onStack.add(c)) return;

        List<EClass> classes = g.classes();
        EClass ec = null;
        for (EClass k : classes) if (k.id() == c) { ec = k; break; }
        if (ec == null) { onStack.remove(c); return; }

        for (ENode n : ec.nodes()) {
            if (out.size() >= maxResults) break;

            if (n.op() == Op.VAR)   { out.add(new VarExpr(n.label()));   continue; }
            if (n.op() == Op.CONST) { out.add(new ConstExpr(Long.parseLong(n.label()))); continue; }

            if (n.op() == Op.NEG) {
                Set<Expr> kids = new LinkedHashSet<>();
                expand(g, n.children().get(0), depth + 1, maxDepth, maxResults, onStack, kids);
                for (Expr k : kids) { out.add(new UnaryExpr(Op.NEG, k)); if (out.size() >= maxResults) break; }
                continue;
            }

            Set<Expr> lefts  = new LinkedHashSet<>();
            Set<Expr> rights = new LinkedHashSet<>();
            expand(g, n.children().get(0), depth + 1, maxDepth, maxResults, onStack, lefts);
            expand(g, n.children().get(1), depth + 1, maxDepth, maxResults, onStack, rights);

            outer:
            for (Expr l : lefts) {
                for (Expr r : rights) {
                    out.add(new BinaryExpr(n.op(), l, r));
                    if (out.size() >= maxResults) break outer;
                }
            }
        }
        onStack.remove(c);
    }
}