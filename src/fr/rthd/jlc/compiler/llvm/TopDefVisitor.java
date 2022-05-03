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
     * Visit only classes or only functions. Used to ensure that a function
     * defined before a class don't call class methods
     */
    private final boolean _classOnly;

    /**
     * Constructor
     * @param classOnly Class only or method only
     */
    public TopDefVisitor(boolean classOnly) {
        _classOnly = classOnly;
    }

    /**
     * Top function definition
     * @param p Top function definition
     * @param env Environment
     */
    @Override
    public Void visit(TopFnDef p, EnvCompiler env) {
        if (!_classOnly) {
            p.funcdef_.accept(new FuncDefVisitor(), env);
        }
        return null;
    }

    /**
     * Top class definition
     * @param p Top class definition
     * @param env Environment
     */
    @Override
    public Void visit(TopClsDef p, EnvCompiler env) {
        if (_classOnly) {
            p.classdef_.accept(new ClassDefVisitor(), env);
        }
        return null;
    }
}
