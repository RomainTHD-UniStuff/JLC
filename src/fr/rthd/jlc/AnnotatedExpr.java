package fr.rthd.jlc;

import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EString;
import javalette.Absyn.Expr;

/**
 * Annotated expression
 * @param <T> Parent expression type
 * @author RomainTHD
 */
public class AnnotatedExpr<T extends Expr> extends Expr {
    /**
     * Parent expression
     */
    public final T parentExp;

    /**
     * Expression type
     */
    public TypeCode type;

    public AnnotatedExpr(TypeCode expType, T parentExp) {
        this.type = expType;
        this.parentExp = parentExp;
    }

    /**
     * Get the default value for the expression type
     * @param type Expression type
     * @return Default expression
     */
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
                throw new IllegalArgumentException(
                    "Unhandled type: " +
                    type
                );
        }
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        // Call the parent accept method
        return parentExp.accept(v, arg);
    }
}
