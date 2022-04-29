package fr.rthd.jlc.compiler.llvm;

import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import org.jetbrains.annotations.NonNls;

/**
 * Top definition visitor
 * @author RomainTHD
 */
@NonNls
class TopDefVisitor implements TopDef.Visitor<Void, EnvCompiler> {
    /**
     * Top function definition
     * @param p Top function definition
     * @param env Environment
     */
    @Override
    public Void visit(TopFnDef p, EnvCompiler env) {
        return p.funcdef_.accept(new FuncDefVisitor(), env);
    }

    /**
     * Top class definition
     * @param p Top class definition
     * @param env Environment
     */
    @Override
    public Void visit(TopClsDef p, EnvCompiler env) {
        return p.classdef_.accept(new ClassDefVisitor(), env);
    }
}
