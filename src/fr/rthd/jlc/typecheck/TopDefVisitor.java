package fr.rthd.jlc.typecheck;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class TopDefVisitor implements TopDef.Visitor<TopDef, EnvTypecheck> {
    public TopFnDef visit(TopFnDef p, EnvTypecheck env) {
        return new TopFnDef(
            p.funcdef_.accept(new FuncDefVisitor(), env)
        );
    }

    public TopClsDef visit(TopClsDef p, EnvTypecheck env) {
        return new TopClsDef(
            p.classdef_.accept(new ClassDefVisitor(), env)
        );
    }
}
