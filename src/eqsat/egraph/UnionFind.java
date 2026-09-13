package eqsat.egraph;

/** Disjoint-set with path compression and union by rank. */
public final class UnionFind {

    private int[] parent = new int[0];
    private int[] rank = new int[0];
    private int count = 0;

    public int makeSet() {
        int id = parent.length;
        parent = java.util.Arrays.copyOf(parent, id + 1);
        rank = java.util.Arrays.copyOf(rank, id + 1);
        parent[id] = id;
        rank[id] = 0;
        count++;
        return id;
    }

    public int find(int x) {
        int root = x;
        while (parent[root] != root) root = parent[root];
        // path compression
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** @return the new root, or the existing root if they were already equal. */
    public int union(int a, int b) {
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) return ra;
        if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;
        if (rank[ra] == rank[rb]) rank[ra]++;
        count--;
        return ra;
    }

    public int classCount() { return count; }
    public int capacity() { return parent.length; }
    public int parentOf(int id) { return parent[id]; }
    public int rankOf(int id) { return rank[id]; }
}