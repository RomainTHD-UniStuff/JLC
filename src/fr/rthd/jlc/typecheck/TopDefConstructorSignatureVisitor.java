package fr.rthd.jlc.typecheck;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import org.jetbrains.annotations.NonNls;

/**
 * Top-level definition visitor for constructor only
 * @author RomainTHD
 */
@NonNls
class TopDefConstructorSignatureVisitor implements TopDef.Visitor<Void, EnvTypecheck> {
    /**
     * Top function definition
     * @param p Top function definition
     * @param env Environment
     */
    @Override
    public Void visit(TopFnDef p, EnvTypecheck env) {
        return null;
    }

    /**
     * Top class definition
     * @param p Top class definition
     * @param env Environment
     */
    @Override
    public Void visit(TopClsDef p, EnvTypecheck env) {
        return p.classdef_.accept(new ClassDefSignatureVisitor(true), env);
    }
}
