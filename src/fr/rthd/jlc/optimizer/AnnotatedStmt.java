package fr.rthd.jlc.optimizer;

import javalette.Absyn.Stmt;

/**
 * Annotated statement
 * @param <T> Statement used
 * @author RomainTHD
 */
public class AnnotatedStmt<T extends Stmt> extends Stmt {
    /**
     * Parent statement
     */
    private final T _parentStmt;

    /**
     * Returning statement or not
     */
    private final boolean _doesReturn;

    public AnnotatedStmt(T parentStmt, boolean doesReturn) {
        this._parentStmt = parentStmt;
        this._doesReturn = doesReturn;
    }

    public AnnotatedStmt(T parentStmt) {
        this(parentStmt, false);
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        // Call parent statement accept method
        return _parentStmt.accept(v, arg);
    }

    public boolean doesReturn() {
        return _doesReturn;
    }

    public T getParentStmt() {
        return _parentStmt;
    }
}
