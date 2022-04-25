package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.ListMember;
import javalette.Absyn.Member;

import java.util.HashMap;
import java.util.Map;

class ClassDefVisitor implements ClassDef.Visitor<ClassDef, EnvTypecheck> {
    public ClassDef visit(ClsDef p, EnvTypecheck env) {
        ClassType c = env.lookupClass(p.ident_);

        ListMember members = new ListMember();

        Map<String, FunType> classFunctions = new HashMap<>();
        for (FunType f : c.getAllMethods()) {
            classFunctions.put(f.name, f);
        }
        env.setClassFunctions(classFunctions);
        env.setCurrentClass(c);
        env.enterScope();

        for (Attribute a : c.getAllAttributes()) {
            env.insertVar(a.name, a.type);
        }

        for (Member m : p.listmember_) {
            members.add(m.accept(new MemberVisitor(), env));
        }

        env.leaveScope();
        env.setCurrentClass(null);
        env.setClassFunctions(null);
        return new ClsDef(
            p.ident_,
            p.classinheritance_,
            members
        );
    }
}
