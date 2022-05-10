package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import javalette.Absyn.Div;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EMul;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.Times;

class MulOpVisitor implements MulOp.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
    private final AnnotatedExpr<?> _left;
    private final AnnotatedExpr<?> _right;

    public MulOpVisitor(AnnotatedExpr<?> left, AnnotatedExpr<?> right) {
        _left = left;
        _right = right;
    }

    public AnnotatedExpr<?> visit(Times p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), Optimizer.operatorAction(
            _left,
            _right,
            (l, r) -> new ELitInt(l * r),
            (l, r) -> new ELitDoub(l * r),
            null,
            (l, r) -> new EMul(l, new Times(), r)
        ));
    }

    public AnnotatedExpr<?> visit(Div p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), Optimizer.operatorAction(
            _left,
            _right,
            (l, r) -> new ELitInt(l / r),
            (l, r) -> new ELitDoub(l / r),
            null,
            (l, r) -> new EMul(l, new Div(), r)
        ));
    }

    public AnnotatedExpr<?> visit(Mod p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), Optimizer.operatorAction(
            _left,
            _right,
            (l, r) -> new ELitInt(l % r),
            (l, r) -> new ELitDoub(l % r),
            null,
            (l, r) -> new EMul(l, new Mod(), r)
        ));
    }
}
