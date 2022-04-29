package fr.rthd.jlc.optimizer;

import javalette.Absyn.Stmt;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Annotated statement
 * @param <T> Statement used
 * @author RomainTHD
 */
@NonNls
public class AnnotatedStmt<T extends Stmt> extends Stmt {
    /**
     * Parent statement
     */
    @NotNull
    private final T _parentStmt;

    /**
     * Returning statement or not
     */
    private final boolean _doesReturn;

    public AnnotatedStmt(@NotNull T parentStmt, boolean doesReturn) {
        _parentStmt = parentStmt;
        _doesReturn = doesReturn;
    }

    public AnnotatedStmt(@NotNull T parentStmt) {
        this(parentStmt, false);
    }

    @Nullable
    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        // Call parent statement accept method
        return _parentStmt.accept(v, arg);
    }

    @Contract(pure = true)
    public boolean doesReturn() {
        return _doesReturn;
    }

    @Contract(pure = true)
    @NotNull
    public T getParentStmt() {
        return _parentStmt;
    }
}
