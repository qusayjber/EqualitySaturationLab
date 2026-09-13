package eqsat.rewrite;

public record PatVar(String name) implements Pattern {
    public PatVar {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("pattern variable name");
    }
    @Override public String render() { return "?" + name; }
    @Override public String toString() { return render(); }
}