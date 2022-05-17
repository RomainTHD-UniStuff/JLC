package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import javalette.Absyn.AttrMember;
import javalette.Absyn.EVar;
import javalette.Absyn.FnMember;
import javalette.Absyn.Member;

class MemberVisitor implements Member.Visitor<Member, EnvOptimizer> {
    private final boolean _attributeOnly;

    public MemberVisitor(boolean attributeOnly) {
        _attributeOnly = attributeOnly;
    }

    @Override
    public Member visit(FnMember p, EnvOptimizer env) {
        if (_attributeOnly) {
            return null;
        } else {
            return new FnMember(p.funcdef_.accept(new FuncDefVisitor(), env));
        }
    }

    @Override
    public Member visit(AttrMember p, EnvOptimizer env) {
        if (_attributeOnly) {
            TypeCode t = p.type_.accept(new TypeVisitor(), null);
            env.insertVar(p.ident_, new AnnotatedExpr<>(
                t,
                new EVar(p.ident_)
            ));
            return p;
        } else {
            return null;
        }
    }
}
