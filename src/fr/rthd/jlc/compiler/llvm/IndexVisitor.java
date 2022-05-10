package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.compiler.OperationItem;
import javalette.Absyn.Index;
import javalette.Absyn.SIndex;

/**
 * Index visitor
 * @author RomainTHD
 */
class IndexVisitor implements Index.Visitor<OperationItem, EnvCompiler> {
    @Override
    public OperationItem visit(SIndex p, EnvCompiler env) {
        return p.expr_.accept(new ExprVisitor(), env);
    }
}
