package fr.rthd.jlc.optimizer;

import javalette.Absyn.Stmt;

public class AnnotatedStmt<T extends Stmt> extends Stmt {
    public final T parentStmt;

    private boolean _doesReturn;

    public AnnotatedStmt(T parentStmt) {
        this.parentStmt = parentStmt;
    }

    @Override
    public <R, A> R accept(Visitor<R, A> v, A arg) {
        return parentStmt.accept(v, arg);
    }
}
