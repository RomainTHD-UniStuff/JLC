package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.typechecker.exception.InvalidIndexTypeException;
import javalette.Absyn.Index;
import javalette.Absyn.SIndex;
import org.jetbrains.annotations.NonNls;

/**
 * Index visitor
 */
@NonNls
class IndexVisitor implements Index.Visitor<Index, EnvTypecheck> {
    /**
     * Index
     */
    private final int _idx;

    /**
     * Constructor
     * @param idx Index
     */
    public IndexVisitor(int idx) {
        _idx = idx;
    }

    @Override
    public Index visit(SIndex i, EnvTypecheck env) {
        AnnotatedExpr<?> e = i.expr_.accept(new ExprVisitor(), env);
        if (e.getType() != TypeCode.CInt) {
            throw new InvalidIndexTypeException(e.getType(), _idx);
        }
        return new SIndex(e);
    }
}
