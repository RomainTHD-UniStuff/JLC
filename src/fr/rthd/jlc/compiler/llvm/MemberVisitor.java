package fr.rthd.jlc.compiler.llvm;

import javalette.Absyn.AttrMember;
import javalette.Absyn.FnMember;
import javalette.Absyn.Member;
import org.jetbrains.annotations.NonNls;

/**
 * Class member visitor
 * @author RomainTHD
 */
@NonNls
class MemberVisitor implements Member.Visitor<Void, EnvCompiler> {
    /**
     * Function member
     * @param p Function member
     * @param env Environment
     */
    @Override
    public Void visit(FnMember p, EnvCompiler env) {
        return p.funcdef_.accept(new FuncDefVisitor(), env);
    }

    /**
     * Attribute member
     * @param p Attribute member
     * @param env Environment
     */
    @Override
    public Void visit(AttrMember p, EnvCompiler env) {
        // Nothing to be done for attributes
        return null;
    }
}
