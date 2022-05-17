package fr.rthd.jlc.optimizer;

import javalette.Absyn.Index;
import javalette.Absyn.SIndex;

class IndexVisitor implements Index.Visitor<Index, EnvOptimizer> {
    @Override
    public Index visit(SIndex p, EnvOptimizer env) {
        return new SIndex(p.expr_.accept(new ExprVisitor(), env));
    }
}
