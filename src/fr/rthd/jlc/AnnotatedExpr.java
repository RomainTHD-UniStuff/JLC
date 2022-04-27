package fr.rthd.jlc;

import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EString;
import javalette.Absyn.Expr;

import static fr.rthd.jlc.TypeCode.CBool;
import static fr.rthd.jlc.TypeCode.CDouble;
import static fr.rthd.jlc.TypeCode.CInt;
import static fr.rthd.jlc.TypeCode.CString;

/**
 * Annotated expression
 * @param <T> Parent expression type
 * @author RomainTHD
 */
public class AnnotatedExpr<T extends Expr> extends Expr {
    /**
     * Parent expression
     */
    private final T _parentExp;

    /**
     * Expression type
     */
    private final TypeCode _type;

    public AnnotatedExpr(TypeCode expType, T parentExp) {
        _type = expType;
        _parentExp = parentExp;
    }

    /**
     * Get the default value for the expression type
     * @param type Expression type
     * @return Default expression
     */
    public static AnnotatedExpr<Expr> getDefaultValue(TypeCode type) {
        if (CInt.equals(type)) {
            return new AnnotatedExpr<>(type, new ELitInt(0));
        } else if (CDouble.equals(type)) {
            return new AnnotatedExpr<>(type, new ELitDoub(0.0));
        } else if (CBool.equals(type)) {
            return new AnnotatedExpr<>(type, new ELitFalse());
        } else if (CString.equals(type)) {
            return new AnnotatedExpr<>(type, new EString(""));
        } else {
            throw new IllegalArgumentException(
                "Unhandled type: " +
                type.getRealName()
            );
        }
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        // Call the parent accept method
        return _parentExp.accept(v, arg);
    }

    public T getParentExp() {
        return _parentExp;
    }

    public TypeCode getType() {
        return _type;
    }
}
