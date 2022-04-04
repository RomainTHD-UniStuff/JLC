package fr.rthd.jlc;

import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EString;
import javalette.Absyn.Expr;

public class AnnotatedExpr<T extends Expr> extends Expr {
    public final T parentExp;
    public TypeCode type;

    public static AnnotatedExpr<Expr> getDefaultValue(TypeCode type) {
        switch (type) {
            case CInt:
                return new AnnotatedExpr<>(type, new ELitInt(0));
            case CDouble:
                return new AnnotatedExpr<>(type, new ELitDoub(0.0));
            case CBool:
                return new AnnotatedExpr<>(type, new ELitFalse());
            case CString:
                return new AnnotatedExpr<>(type, new EString(""));
            case CVoid:
            default:
                throw new UnsupportedOperationException(
                    "Unhandled type: " +
                    type
                );
        }
    }


    public AnnotatedExpr(TypeCode expType, T parentExp) {
        this.type = expType;
        this.parentExp = parentExp;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        return parentExp.accept(v, arg);
    }
}
