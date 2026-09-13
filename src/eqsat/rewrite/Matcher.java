package eqsat.rewrite;

import eqsat.egraph.EGraph;
import eqsat.egraph.ENode;
import eqsat.model.Op;

import java.util.*;

/** Pattern matching against E-Classes + RHS instantiation back into the E-Graph. */
public final class Matcher {

    private Matcher() {}

    // ------------------------------------------------------------------ matching

    public static List<Map<String, Integer>> match(Pattern p, int classId, EGraph g) {
        List<Map<String, Integer>> out = new ArrayList<>();
        matchInto(p, classId, g, new HashMap<>(), out);
        return dedupe(out);
    }

    private static void matchInto(Pattern p, int classId, EGraph g,
                                  Map<String, Integer> subst,
                                  List<Map<String, Integer>> out) {
        int c = g.find(classId);

        if (p instanceof PatVar v) {
            Integer bound = subst.get(v.name());
            if (bound == null) {
                Map<String, Integer> next = new HashMap<>(subst);
                next.put(v.name(), c);
                out.add(next);
            } else if (g.find(bound) == c) {
                out.add(subst);
            }
            return;
        }

        if (p instanceof PatLeaf leaf) {
            for (ENode n : g.nodesOf(c)) {
                if (n.op() == leaf.op() && Objects.equals(n.label(), leaf.label())) {
                    out.add(subst);
                }
            }
            return;
        }

        PatNode pn = (PatNode) p;
        for (ENode n : g.nodesOf(c)) {
            if (n.op() != pn.op()) continue;
            if (n.children().size() != pn.children().size()) continue;

            List<Map<String, Integer>> current = new ArrayList<>();
            current.add(subst);
            for (int i = 0; i < pn.children().size() && !current.isEmpty(); i++) {
                List<Map<String, Integer>> next = new ArrayList<>();
                for (Map<String, Integer> s : current) {
                    matchInto(pn.children().get(i), n.children().get(i), g, s, next);
                }
                current = next;
            }
            out.addAll(current);
        }
    }

    private static List<Map<String, Integer>> dedupe(List<Map<String, Integer>> in) {
        LinkedHashMap<String, Map<String, Integer>> seen = new LinkedHashMap<>();
        for (Map<String, Integer> m : in) {
            List<String> keys = new ArrayList<>(m.keySet());
            Collections.sort(keys);
            StringBuilder sb = new StringBuilder();
            for (String k : keys) sb.append(k).append('=').append(m.get(k)).append(';');
            seen.putIfAbsent(sb.toString(), m);
        }
        return new ArrayList<>(seen.values());
    }

    // ------------------------------------------------------------------ instantiation

    public static int instantiate(Pattern p, Map<String, Integer> subst, EGraph g) {
        if (p instanceof PatVar v) {
            Integer id = subst.get(v.name());
            if (id == null) throw new IllegalStateException("unbound pattern variable ?" + v.name());
            return g.find(id);
        }
        if (p instanceof PatLeaf leaf) {
            return leaf.op() == Op.CONST
                    ? g.add(ENode.constant(Long.parseLong(leaf.label())))
                    : g.add(ENode.var(leaf.label()));
        }
        PatNode pn = (PatNode) p;
        List<Integer> kids = new ArrayList<>(pn.children().size());
        for (Pattern child : pn.children()) kids.add(instantiate(child, subst, g));
        return g.add(ENode.of(pn.op(), kids));
    }
}