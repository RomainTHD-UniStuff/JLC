package fr.rthd.jlc;

import javalette.Absyn.Expr;

public class AnnotatedExpr<T extends Expr> extends Expr {
    public final T parentExp;
    public TypeCode type;

    public AnnotatedExpr(TypeCode expType, T parentExp) {
        this.type = expType;
        this.parentExp = parentExp;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        return parentExp.accept(v, arg);
    }
}
