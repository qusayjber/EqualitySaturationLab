package eqsat.extract;

import eqsat.model.*;

import java.util.List;

public final class CostCalculator {
    private CostCalculator() {}

    public static double costOf(Expr e, CostModel cm) {
        if (e instanceof VarExpr)      return cm.combine(Op.VAR, null, List.of());
        if (e instanceof ConstExpr c)  return cm.combine(Op.CONST, Long.toString(c.value()), List.of());
        if (e instanceof UnaryExpr u)  return cm.combine(u.op(), null, List.of(costOf(u.operand(), cm)));
        BinaryExpr b = (BinaryExpr) e;
        return cm.combine(b.op(), null, List.of(costOf(b.left(), cm), costOf(b.right(), cm)));
    }
}