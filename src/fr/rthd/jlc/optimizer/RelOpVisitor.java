package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EQU;
import javalette.Absyn.ERel;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.NE;
import javalette.Absyn.RelOp;

import static fr.rthd.jlc.optimizer.Optimizer.operatorAction;

class RelOpVisitor implements RelOp.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
    private final AnnotatedExpr<?> _left;
    private final AnnotatedExpr<?> _right;

    public RelOpVisitor(AnnotatedExpr<?> left, AnnotatedExpr<?> right) {
        _left = left;
        _right = right;
    }

    public AnnotatedExpr<?> visit(LTH p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> l < r ? new ELitTrue() : new ELitFalse(),
            (l, r) -> l < r ? new ELitTrue() : new ELitFalse(),
            null,
            (l, r) -> new ERel(l, new LTH(), r)
        ));
    }

    public AnnotatedExpr<?> visit(LE p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
            (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
            null,
            (l, r) -> new ERel(l, new LE(), r)
        ));
    }

    public AnnotatedExpr<?> visit(GTH p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> l > r ? new ELitTrue() : new ELitFalse(),
            (l, r) -> l > r ? new ELitTrue() : new ELitFalse(),
            null,
            (l, r) -> new ERel(l, new GTH(), r)
        ));
    }

    public AnnotatedExpr<?> visit(GE p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
            (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
            null,
            (l, r) -> new ERel(l, new GE(), r)
        ));
    }

    public AnnotatedExpr<?> visit(EQU p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> l.equals(r) ? new ELitTrue() : new ELitFalse(),
            (l, r) -> l.equals(r) ? new ELitTrue() : new ELitFalse(),
            (l, r) -> l.equals(r) ? new ELitTrue() : new ELitFalse(),
            (l, r) -> new ERel(l, new EQU(), r)
        ));
    }

    public AnnotatedExpr<?> visit(NE p, EnvOptimizer env) {
        return new AnnotatedExpr<>(_left.getType(), operatorAction(
            _left,
            _right,
            (l, r) -> l.equals(r) ? new ELitFalse() : new ELitTrue(),
            (l, r) -> l.equals(r) ? new ELitFalse() : new ELitTrue(),
            (l, r) -> l.equals(r) ? new ELitFalse() : new ELitTrue(),
            (l, r) -> new ERel(l, new NE(), r)
        ));
    }
}
