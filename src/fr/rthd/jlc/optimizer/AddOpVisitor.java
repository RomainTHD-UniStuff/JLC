package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import javalette.Absyn.AddOp;
import javalette.Absyn.EAdd;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitInt;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;

import static fr.rthd.jlc.optimizer.Optimizer.operatorAction;

class AddOpVisitor implements AddOp.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
    private final AnnotatedExpr<?> _left;
    private final AnnotatedExpr<?> _right;

    public AddOpVisitor(AnnotatedExpr<?> left, AnnotatedExpr<?> right) {
        _left = left;
        _right = right;
    }

    public AnnotatedExpr<?> visit(Plus p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> new ELitInt(l + r),
            (l, r) -> new ELitDoub(l + r),
            null,
            (l, r) -> new EAdd(l, new Plus(), r)
        ));
    }

    public AnnotatedExpr<?> visit(Minus p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> new ELitInt(l - r),
            (l, r) -> new ELitDoub(l - r),
            null,
            (l, r) -> new EAdd(l, new Minus(), r)
        ));
    }
}
