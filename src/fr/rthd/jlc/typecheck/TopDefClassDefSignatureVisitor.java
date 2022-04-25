package fr.rthd.jlc.typecheck;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class TopDefClassDefSignatureVisitor implements TopDef.Visitor<Void, EnvTypecheck> {
    public Void visit(TopFnDef p, EnvTypecheck env) {
        return null;
    }

    public Void visit(TopClsDef p, EnvTypecheck env) {
        return p.classdef_.accept(
            new ClassDefSignatureVisitor(false),
            env
        );
    }
}
