package eqsat.rewrite;

public sealed interface Pattern permits PatVar, PatLeaf, PatNode {
    String render();
}