package fr.rthd.jlc.optimizer;

import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.ListMember;
import javalette.Absyn.Member;

class ClassDefVisitor implements ClassDef.Visitor<ClassDef, EnvOptimizer> {
    @Override
    public ClassDef visit(ClsDef p, EnvOptimizer env) {
        ClassTypeOptimizer c = env.lookupClass(p.ident_);
        assert c != null;
        env.setCurrentClass(c);
        ListMember members = new ListMember();
        for (Member member : p.listmember_) {
            members.add(member.accept(new MemberVisitor(), env));
        }
        env.setCurrentClass(null);
        return new ClsDef(
            p.ident_,
            p.classinheritance_,
            members
        );
    }
}
