package fr.rthd.jlc.typecheck;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import org.jetbrains.annotations.NonNls;

/**
 * Top-level definition visitor
 * @author RomainTHD
 */
@NonNls
class TopDefVisitor implements TopDef.Visitor<TopDef, EnvTypecheck> {
    /**
     * Top-level function definition
     * @param p Top-level function definition
     * @param env Environment
     * @return Top-level function definition
     */
    @Override
    public TopFnDef visit(TopFnDef p, EnvTypecheck env) {
        return new TopFnDef(
            p.funcdef_.accept(new FuncDefVisitor(), env)
        );
    }

    /**
     * Top-level class definition
     * @param p Top-level class definition
     * @param env Environment
     * @return Top-level class definition
     */
    @Override
    public TopClsDef visit(TopClsDef p, EnvTypecheck env) {
        return new TopClsDef(
            p.classdef_.accept(new ClassDefVisitor(), env)
        );
    }
}
