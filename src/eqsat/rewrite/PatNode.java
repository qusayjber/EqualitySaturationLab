package eqsat.rewrite;

import eqsat.model.Op;

import java.util.List;
import java.util.stream.Collectors;

public record PatNode(Op op, List<Pattern> children) implements Pattern {
    public PatNode {
        if (op.arity() != children.size())
            throw new IllegalArgumentException(op + " expects " + op.arity() + " children");
        children = List.copyOf(children);
    }
    @Override public String render() {
        return op.displayName() + "(" +
                children.stream().map(Pattern::render).collect(Collectors.joining(", ")) + ")";
    }
    @Override public String toString() { return render(); }
}