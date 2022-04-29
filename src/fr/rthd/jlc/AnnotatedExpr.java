package fr.rthd.jlc;

import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EString;
import javalette.Absyn.Expr;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fr.rthd.jlc.TypeCode.CBool;
import static fr.rthd.jlc.TypeCode.CDouble;
import static fr.rthd.jlc.TypeCode.CInt;
import static fr.rthd.jlc.TypeCode.CString;

/**
 * Annotated expression
 * @param <T> Parent expression type
 * @author RomainTHD
 */
@NonNls
public class AnnotatedExpr<T extends Expr> extends Expr {
    /**
     * Parent expression
     */
    @NotNull
    private final T _parentExp;

    /**
     * Expression type
     */
    @NotNull
    private final TypeCode _type;

    public AnnotatedExpr(@NotNull TypeCode expType, @NotNull T parentExp) {
        _type = expType;
        _parentExp = parentExp;
    }

    /**
     * Get the default value for the expression type
     * @param type Expression type
     * @return Default expression
     */
    @NotNull
    public static AnnotatedExpr<Expr> getDefaultValue(@NotNull TypeCode type) {
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

    @Nullable
    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        // Call the parent accept method
        return _parentExp.accept(v, arg);
    }

    @Contract(pure = true)
    @NotNull
    public T getParentExp() {
        return _parentExp;
    }

    @Contract(pure = true)
    @NotNull
    public TypeCode getType() {
        return _type;
    }
}
