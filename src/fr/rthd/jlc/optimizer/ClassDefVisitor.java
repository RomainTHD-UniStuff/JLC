package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.EVar;
import javalette.Absyn.ListMember;
import javalette.Absyn.Member;

class ClassDefVisitor implements ClassDef.Visitor<ClassDef, EnvOptimizer> {
    @Override
    public ClassDef visit(ClsDef p, EnvOptimizer env) {
        ClassTypeOptimizer c = env.lookupClass(p.ident_);
        assert c != null;
        env.enterScope();
        env.setCurrentClass(c);
        ListMember members = new ListMember();

        env.insertVar(
            "self",
            new AnnotatedExpr<>(c.getType(), new EVar("self"))
        );

        for (Member member : p.listmember_) {
            // We add all attributes
            Member newMember = member.accept(new MemberVisitor(true), env);
            if (newMember != null) {
                members.add(newMember);
            }
        }

        for (Member member : p.listmember_) {
            // We add all methods
            Member newMember = member.accept(new MemberVisitor(false), env);
            if (newMember != null) {
                members.add(newMember);
            }
        }

        env.setCurrentClass(null);
        env.leaveScope();
        return new ClsDef(
            p.ident_,
            p.classinheritance_,
            members
        );
    }
}
