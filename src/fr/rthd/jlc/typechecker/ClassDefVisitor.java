package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.ListMember;
import javalette.Absyn.Member;
import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.Map;

/**
 * Class definition visitor
 * @author RomainTHD
 */
@NonNls
class ClassDefVisitor implements ClassDef.Visitor<ClassDef, EnvTypecheck> {
    /**
     * Class definition
     * @param p Class definition
     * @param env Environment
     * @return Class definition
     */
    @Override
    public ClassDef visit(ClsDef p, EnvTypecheck env) {
        ClassType<?> c = env.lookupClass(p.ident_);
        assert c != null;

        ListMember members = new ListMember();

        Map<String, FunType> classFunctions = new HashMap<>();
        for (FunType f : c.getAllMethods()) {
            classFunctions.put(f.getName(), f);
        }
        env.setClassFunctions(classFunctions);
        env.setCurrentClass(c);
        env.enterScope();

        for (Attribute a : c.getAllAttributes()) {
            env.insertVar(a.getName(), a.getType());
        }

        env.insertVar("self", c.getType());

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
