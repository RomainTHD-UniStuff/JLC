package fr.rthd.jlc.typecheck;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class TopDefSignatureVisitor implements TopDef.Visitor<Void, EnvTypecheck> {
    public Void visit(TopFnDef p, EnvTypecheck env) {
        return p.funcdef_.accept(new FuncDefSignatureVisitor(), env);
    }

    public Void visit(TopClsDef p, EnvTypecheck env) {
        return p.classdef_.accept(new ClassDefSignatureVisitor(true), env);
    }
}
