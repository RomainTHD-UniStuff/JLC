import javalette.Absyn.Expr;

public class ExpCustom extends Expr {
    public final Expr parentExp;
    public TypeCode type;
    public TypeCode coertTo;

    public ExpCustom(TypeCode expType, Expr parentExp) {
        this.type = expType;
        this.parentExp = parentExp;
        this.coertTo = null;
    }

    public ExpCustom maybeCoertTo(TypeCode coertTo) {
        if (coertTo != type) {
            this.coertTo = coertTo;
        }
        return this;
    }

    public boolean needsCoercion() {
        return coertTo != null;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        return parentExp.accept(v, arg);
    }

    public void markAsCoerced() {
        if (this.needsCoercion()) {
            this.type = this.coertTo;
        }
    }
}
