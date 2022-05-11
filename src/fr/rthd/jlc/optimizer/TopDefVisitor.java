package fr.rthd.jlc.optimizer;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class TopDefVisitor implements TopDef.Visitor<TopDef, EnvOptimizer> {
    public TopFnDef visit(TopFnDef p, EnvOptimizer env) {
        return new TopFnDef(p.funcdef_.accept(new FuncDefVisitor(), env));
    }

    public TopClsDef visit(TopClsDef p, EnvOptimizer env) {
        return new TopClsDef(p.classdef_.accept(new ClassDefVisitor(), env));
    }
}
