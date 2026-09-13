package eqsat.egraph;

import eqsat.model.Op;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An E-Node references E-Class ids, never AST objects.
 */
public record ENode(Op op, String label, List<Integer> children) {

    public ENode {
        children = List.copyOf(children);
    }

    public static ENode var(String name) { return new ENode(Op.VAR, name, List.of()); }
    public static ENode constant(long value) { return new ENode(Op.CONST, Long.toString(value), List.of()); }

    public static ENode of(Op op, int... kids) {
        List<Integer> list = new java.util.ArrayList<>(kids.length);
        for (int k : kids) list.add(k);
        return new ENode(op, null, list);
    }

    public static ENode of(Op op, List<Integer> kids) { return new ENode(op, null, kids); }

    /** Compact textual form used in the UI, e.g. {@code Add(3, 5)} or {@code Var(a)}. */
    public String render() {
        return switch (op) {
            case VAR -> "Var(" + label + ")";
            case CONST -> "Const(" + label + ")";
            case NEG -> "Neg(" + children.get(0) + ")";
            default -> op.displayName() + "(" +
                    children.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")";
        };
    }

    public boolean isLeaf() { return op.isLeaf(); }
}