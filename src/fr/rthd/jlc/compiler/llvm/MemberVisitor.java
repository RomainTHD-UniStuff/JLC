package fr.rthd.jlc.compiler.llvm;

import javalette.Absyn.AttrMember;
import javalette.Absyn.FnMember;
import javalette.Absyn.Member;

class MemberVisitor implements Member.Visitor<Void, EnvCompiler> {
    public Void visit(FnMember p, EnvCompiler env) {
        return p.funcdef_.accept(new FuncDefVisitor(), env);
    }

    public Void visit(AttrMember p, EnvCompiler env) {
        return null;
    }
}
