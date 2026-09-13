package eqsat.model;

public final class Exprs {
    private Exprs() {}

    public static int size(Expr e) {
        if (e instanceof VarExpr || e instanceof ConstExpr) return 1;
        if (e instanceof UnaryExpr u) return 1 + size(u.operand());
        BinaryExpr b = (BinaryExpr) e;
        return 1 + size(b.left()) + size(b.right());
    }

    public static int depth(Expr e) {
        if (e instanceof VarExpr || e instanceof ConstExpr) return 1;
        if (e instanceof UnaryExpr u) return 1 + depth(u.operand());
        BinaryExpr b = (BinaryExpr) e;
        return 1 + Math.max(depth(b.left()), depth(b.right()));
    }
}