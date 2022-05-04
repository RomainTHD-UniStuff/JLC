package fr.rthd.jlc;

import javalette.Absyn.LValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Annotated expression
 * @param <T> Parent expression type
 * @author RomainTHD
 */
@NonNls
public class AnnotatedLValue<T extends LValue> extends LValue {
    /**
     * Parent expression
     */
    @NotNull
    private final T _parentExp;

    /**
     * Expression type
     */
    @NotNull
    private final String _baseName;

    @Nullable
    private final String _methodName;

    /**
     * Constructor
     * @param expType Expression type
     * @param parentExp Parent expression
     */
    public AnnotatedLValue(@NotNull String baseName, @NotNull T parentExp) {
        this(baseName, parentExp, null);
    }

    /**
     * Constructor
     * @param expType Expression type
     * @param parentExp Parent expression
     */
    public AnnotatedLValue(
        @NotNull String baseName,
        @NotNull T parentExp,
        @Nullable String methodName
    ) {
        _baseName = baseName;
        _parentExp = parentExp;
        _methodName = methodName;
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
    public String getBaseName() {
        return _baseName;
    }

    @Contract(pure = true)
    @NotNull
    public String getMethodName() {
        return Objects.requireNonNullElse(_methodName, _baseName);
    }
}
