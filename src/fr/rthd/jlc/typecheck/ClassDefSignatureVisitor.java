package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.DuplicateFieldException;
import javalette.Absyn.Arg;
import javalette.Absyn.AttrMember;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.FnDef;
import javalette.Absyn.FnMember;
import javalette.Absyn.HBase;
import javalette.Absyn.HExtends;
import javalette.Absyn.Member;

import java.util.LinkedList;
import java.util.List;

class ClassDefSignatureVisitor implements ClassDef.Visitor<Void, EnvTypecheck> {
    private final boolean _checkMethods;

    public ClassDefSignatureVisitor(boolean checkMethods) {
        this._checkMethods = checkMethods;
    }

    private void defOnly(ClsDef p, EnvTypecheck env) {
        String superclass;
        if (p.classinheritance_ instanceof HBase) {
            superclass = null;
        } else if (p.classinheritance_ instanceof HExtends) {
            superclass = ((HExtends) p.classinheritance_).ident_;
        } else {
            throw new IllegalArgumentException(String.format(
                "Unknown interhitance type: %s",
                p.classinheritance_.getClass().getName()
            ));
        }

        env.insertClass(new ClassType(
            p.ident_,
            superclass
        ));
    }

    private void addMethods(ClsDef p, EnvTypecheck env) {
        ClassType c = env.lookupClass(p.ident_);

        for (Member m : p.listmember_) {
            if (m instanceof FnMember) {
                FnDef f = (FnDef) ((FnMember) m).funcdef_;
                List<FunArg> args = new LinkedList<>();
                for (Arg arg : f.listarg_) {
                    args.add(arg.accept(new ArgVisitor(), null));
                }
                if (c.hasMethod(f.ident_)) {
                    throw new DuplicateFieldException(
                        f.ident_,
                        c.name,
                        "method"
                    );
                }
                c.addMethod(new FunType(
                    f.type_.accept(new TypeVisitor(), null),
                    f.ident_,
                    args
                ));
            } else if (m instanceof AttrMember) {
                AttrMember a = (AttrMember) m;
                if (c.hasAttribute(a.ident_)) {
                    throw new DuplicateFieldException(
                        a.ident_,
                        c.name,
                        "attribute"
                    );
                }
                c.addAttribute(new Attribute(
                    a.type_.accept(new TypeVisitor(), null),
                    a.ident_
                ));
            } else {
                throw new IllegalArgumentException(
                    "Unknown member type: " + m.getClass().getName()
                );
            }
        }
    }

    public Void visit(ClsDef p, EnvTypecheck env) {
        // We need to visit the class definition twice: first we list all
        //  the classes, then we fill them with their attributes and
        //  methods. This is because a function returning an object could
        //  see this object not recognized initially

        if (_checkMethods) {
            addMethods(p, env);
        } else {
            defOnly(p, env);
        }

        return null;
    }
}
