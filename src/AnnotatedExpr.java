import javalette.Absyn.Expr;

public class AnnotatedExpr<T extends Expr> extends Expr {
    public final T parentExp;
    public TypeCode type;
    public TypeCode coertTo;

    public AnnotatedExpr(TypeCode expType, T parentExp) {
        this.type = expType;
        this.parentExp = parentExp;
        this.coertTo = null;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        return parentExp.accept(v, arg);
    }
}
