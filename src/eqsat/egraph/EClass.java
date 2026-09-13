package eqsat.egraph;

import java.util.List;

/**
 * Immutable snapshot of one equivalence class (only canonical/root classes are exposed).
 */
public record EClass(int id, List<ENode> nodes) {
    public EClass { nodes = List.copyOf(nodes); }

    public int size() { return nodes.size(); }

    public String render() {
        StringBuilder sb = new StringBuilder("EClass #").append(id);
        for (ENode n : nodes) sb.append("\n  ").append(n.render());
        return sb.toString();
    }
}