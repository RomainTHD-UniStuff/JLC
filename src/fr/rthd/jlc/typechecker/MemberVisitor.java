package fr.rthd.jlc.typechecker;

import javalette.Absyn.AttrMember;
import javalette.Absyn.FnMember;
import javalette.Absyn.Member;
import org.jetbrains.annotations.NonNls;

/**
 * Class member visitor
 * @author RomainTHD
 */
@NonNls
class MemberVisitor implements Member.Visitor<Member, EnvTypecheck> {
    /**
     * Function member
     * @param p Function member
     * @param env Environment
     * @return Function member
     */
    @Override
    public FnMember visit(FnMember p, EnvTypecheck env) {
        return new FnMember(
            p.funcdef_.accept(new FuncDefVisitor(), env)
        );
    }

    /**
     * Attribute member
     * @param p Attribute member
     * @param env Environment
     * @return Attribute member
     */
    @Override
    public AttrMember visit(AttrMember p, EnvTypecheck env) {
        return new AttrMember(
            p.type_,
            p.ident_
        );
    }
}
