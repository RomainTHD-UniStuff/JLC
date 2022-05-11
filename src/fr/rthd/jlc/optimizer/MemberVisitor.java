package fr.rthd.jlc.optimizer;

import javalette.Absyn.AttrMember;
import javalette.Absyn.FnMember;
import javalette.Absyn.Member;

class MemberVisitor implements Member.Visitor<Member, EnvOptimizer> {
    @Override
    public Member visit(FnMember p, EnvOptimizer env) {
        return new FnMember(p.funcdef_.accept(new FuncDefVisitor(), env));
    }

    @Override
    public Member visit(AttrMember p, EnvOptimizer env) {
        return p;
    }
}
