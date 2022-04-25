package fr.rthd.jlc.compiler;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class TopDefVisitor implements TopDef.Visitor<Void, EnvCompiler> {
    public Void visit(TopFnDef p, EnvCompiler env) {
        return p.funcdef_.accept(new FuncDefVisitor(), env);
    }

    public Void visit(TopClsDef p, EnvCompiler env) {
        return p.classdef_.accept(new ClassDefVisitor(), env);
    }
}
