package eqsat.egraph;

import eqsat.model.*;

import java.util.*;

/**
 * A genuine E-Graph:
 *   • union-find over E-Class ids
 *   • hash-consing of E-Nodes (canonical child ids)
 *   • congruence closure via a rebuild() fixpoint
 */
public final class EGraph {

    private final UnionFind uf = new UnionFind();
    /** index = class id; only root classes hold a meaningful set. */
    private final List<Set<ENode>> classNodes = new ArrayList<>();
    private final Map<ENodeKey, Integer> hashCons = new HashMap<>();

    private long unionOperations;
    private long congruenceMerges;

    // ------------------------------------------------------------------ find / add

    public int find(int id) { return uf.find(id); }

    public int add(ENode node) {
        ENodeKey key = canonicalKey(node);
        Integer existing = hashCons.get(key);
        if (existing != null) {
            int root = uf.find(existing);
            if (!classNodes.get(root).isEmpty()) return root;
        }
        int id = uf.makeSet();
        classNodes.add(new LinkedHashSet<>());
        classNodes.get(id).add(new ENode(key.op(), key.label(), key.children()));
        hashCons.put(key, id);
        return id;
    }

    public int addExpr(Expr e) {
        return switch (e) {
            case VarExpr v     -> add(ENode.var(v.name()));
            case ConstExpr c   -> add(ENode.constant(c.value()));
            case UnaryExpr u   -> add(ENode.of(u.op(), addExpr(u.operand())));
            case BinaryExpr b  -> add(ENode.of(b.op(), addExpr(b.left()), addExpr(b.right())));
        };
    }

    private ENodeKey canonicalKey(ENode n) {
        List<Integer> ch = new ArrayList<>(n.children().size());
        for (int c : n.children()) ch.add(uf.find(c));
        return new ENodeKey(n.op(), n.label(), ch);
    }

    // ------------------------------------------------------------------ union

    public boolean union(int a, int b) {
        int ra = uf.find(a);
        int rb = uf.find(b);
        if (ra == rb) return false;
        int root = uf.union(ra, rb);
        int other = (root == ra) ? rb : ra;
        classNodes.get(root).addAll(classNodes.get(other));
        classNodes.get(other).clear();
        unionOperations++;
        return true;
    }

    private void mergeCongruent(int a, int b) {
        if (union(a, b)) congruenceMerges++;
    }

    // ------------------------------------------------------------------ rebuild

    /**
     * Congruence closure: re-canonicalise every E-Node against the current
     * union-find roots; whenever two E-Nodes in different classes become equal,
     * merge those classes and repeat until a fixpoint is reached.
     *
     * @return true if any merge happened.
     */
    public boolean rebuild() {
        boolean anyChange = false;
        boolean changed = true;
        int guard = 0;

        while (changed && guard++ < 1024) {
            changed = false;
            hashCons.clear();
            List<int[]> pending = new ArrayList<>();

            for (int i = 0; i < classNodes.size(); i++) {
                if (uf.find(i) != i) continue;
                Set<ENode> current = classNodes.get(i);
                if (current.isEmpty()) continue;

                Set<ENode> rebuilt = new LinkedHashSet<>();
                for (ENode n : current) {
                    ENodeKey k = canonicalKey(n);
                    rebuilt.add(new ENode(k.op(), k.label(), k.children()));

                    Integer prev = hashCons.get(k);
                    if (prev == null) {
                        hashCons.put(k, i);
                    } else if (uf.find(prev) != i) {
                        pending.add(new int[]{prev, i});
                    }
                }
                classNodes.set(i, rebuilt);
            }

            for (int[] p : pending) {
                int a = uf.find(p[0]);
                int b = uf.find(p[1]);
                if (a != b) {
                    mergeCongruent(a, b);
                    changed = true;
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    // ------------------------------------------------------------------ queries

    public Set<ENode> nodesOf(int classId) { return classNodes.get(uf.find(classId)); }

    public List<Integer> rootClassIds() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < classNodes.size(); i++) {
            if (uf.find(i) == i && !classNodes.get(i).isEmpty()) out.add(i);
        }
        return out;
    }

    public List<EClass> classes() {
        List<EClass> out = new ArrayList<>();
        for (int i = 0; i < classNodes.size(); i++) {
            if (uf.find(i) != i) continue;
            Set<ENode> ns = classNodes.get(i);
            if (ns.isEmpty()) continue;
            out.add(new EClass(i, new ArrayList<>(ns)));
        }
        return out;
    }

    public int classCount() {
        int n = 0;
        for (int i = 0; i < classNodes.size(); i++)
            if (uf.find(i) == i && !classNodes.get(i).isEmpty()) n++;
        return n;
    }

    public int nodeCount() {
        int n = 0;
        for (int i = 0; i < classNodes.size(); i++)
            if (uf.find(i) == i) n += classNodes.get(i).size();
        return n;
    }

    public long unionOperations() { return unionOperations; }
    public long congruenceMerges() { return congruenceMerges; }
    public int capacity() { return uf.capacity(); }
    public int unionFindParent(int id) { return uf.parentOf(id); }
    public boolean isCanonical(int id) { return uf.find(id) == id; }

    /** Text dump for export. */
    public String dump() {
        StringBuilder sb = new StringBuilder();
        for (EClass c : classes()) sb.append(c.render()).append("\n\n");
        return sb.toString();
    }
}