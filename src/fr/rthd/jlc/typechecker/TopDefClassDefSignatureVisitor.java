package fr.rthd.jlc.typechecker;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import org.jetbrains.annotations.NonNls;

/**
 * Top-level definition for class definition signature visitor
 * @author RomainTHD
 */
@NonNls
class TopDefClassDefSignatureVisitor implements TopDef.Visitor<Void, EnvTypecheck> {
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
        return p.classdef_.accept(
            new ClassDefOnlySignatureVisitor(),
            env
        );
    }
}
