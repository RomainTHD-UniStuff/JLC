package fr.rthd.jlc.compiler;

import fr.rthd.jlc.internal.NotImplementedException;
import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import javalette.Absyn.Void;

class TopDefVisitor implements TopDef.Visitor<Void, EnvCompiler> {
    public Void visit(TopFnDef p, EnvCompiler env) {
        return p.funcdef_.accept(new FuncDefVisitor(), env);
    }

    public Void visit(TopClsDef p, EnvCompiler env) {
        throw new NotImplementedException();
    }
}
