package fr.rthd.jlc;

import fr.rthd.jlc.utils.Value;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EString;
import javalette.Absyn.Expr;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Expression value
     */
    @NotNull
    private final Value _value;

    /**
     * Constructor
     * @param expType Expression type
     * @param parentExp Parent expression
     */
    public AnnotatedExpr(@NotNull TypeCode expType, @NotNull T parentExp) {
        this(expType, parentExp, Value.RValue);
    }

    /**
     * Constructor
     * @param expType Expression type
     * @param parentExp Parent expression
     * @param value Expression value
     * @see Value
     */
    public AnnotatedExpr(
        @NotNull TypeCode expType,
        @NotNull T parentExp,
        @NotNull Value value
    ) {
        _type = expType;
        _parentExp = parentExp;
        _value = value;
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

    /**
     * Accept an expression visitor
     * @param v Visitor
     * @param arg Argument
     * @param <R> Return type
     * @param <A> Argument type
     * @return Visitor return value
     */
    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        // Call the parent accept method
        return _parentExp.accept(v, arg);
    }

    /**
     * @return Parent expression
     */
    @Contract(pure = true)
    @NotNull
    public T getParentExp() {
        return _parentExp;
    }

    /**
     * @return Expression type
     */
    @Contract(pure = true)
    @NotNull
    public TypeCode getType() {
        return _type;
    }

    /**
     * @return Expression value
     * @see Value
     */
    @Contract(pure = true)
    @NotNull
    public Value getValue() {
        return _value;
    }
}
