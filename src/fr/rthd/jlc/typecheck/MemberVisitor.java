package fr.rthd.jlc.typecheck;

import javalette.Absyn.AttrMember;
import javalette.Absyn.FnMember;
import javalette.Absyn.Member;

class MemberVisitor implements Member.Visitor<Member, EnvTypecheck> {
    public FnMember visit(FnMember p, EnvTypecheck env) {
        return new FnMember(
            p.funcdef_.accept(new FuncDefVisitor(), env)
        );
    }

    public AttrMember visit(AttrMember p, EnvTypecheck env) {
        return new AttrMember(
            p.type_,
            p.ident_
        );
    }
}
