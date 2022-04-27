package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.DuplicateFieldException;
import javalette.Absyn.Arg;
import javalette.Absyn.Ass;
import javalette.Absyn.AttrMember;
import javalette.Absyn.Block;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.FnDef;
import javalette.Absyn.FnMember;
import javalette.Absyn.ListArg;
import javalette.Absyn.ListStmt;
import javalette.Absyn.Member;

import java.util.LinkedList;
import java.util.List;

/**
 * We need to visit the class definition twice: first we list all the classes,
 * then we fill them with their attributes and methods (here). This is because a
 * function returning an object could see this object not recognized initially
 * @author RomainTHD
 * @see ClassDefOnlySignatureVisitor
 */
class ClassDefSignatureVisitor implements ClassDef.Visitor<Void, EnvTypecheck> {
    /**
     * Only add the constructor to the class
     */
    private final boolean constructorOnly;

    public ClassDefSignatureVisitor(boolean constructorOnly) {
        this.constructorOnly = constructorOnly;
    }

    /**
     * Add constructor to the current class. Needs to be done after the fields
     * have been added, hence the second visit
     * @param p Class definition
     * @param env Environment
     */
    private void handleConstructor(ClsDef p, EnvTypecheck env) {
        ClassType c = env.lookupClass(p.ident_);

        ListStmt body = new ListStmt();
        for (Attribute attr : c.getAllAttributes()) {
            if (attr.getType().isPrimitive()) {
                body.add(new Ass(
                    attr.getName(),
                    AnnotatedExpr.getDefaultValue(attr.getType())
                ));
            }
        }

        FnDef fdef = new FnDef(
            new javalette.Absyn.Void(),
            c.getConstructorName(),
            new ListArg(),
            new Block(body)
        );

        p.listmember_.add(new FnMember(fdef));
        addMethod(c, fdef);
    }

    private void addMethod(ClassType c, FnDef f) {
        List<FunArg> args = new LinkedList<>();
        for (Arg arg : f.listarg_) {
            args.add(arg.accept(new ArgVisitor(), null));
        }
        if (c.hasMethod(f.ident_)) {
            throw new DuplicateFieldException(
                f.ident_,
                c.getName(),
                "method"
            );
        }
        c.addMethod(new FunType(
            f.type_.accept(new TypeVisitor(), null),
            f.ident_,
            args
        ));
    }

    private void addAttribute(ClassType c, AttrMember a) {
        if (c.hasAttribute(a.ident_)) {
            throw new DuplicateFieldException(
                a.ident_,
                c.getName(),
                "attribute"
            );
        }
        c.addAttribute(new Attribute(
            a.type_.accept(new TypeVisitor(), null),
            a.ident_
        ));
    }

    private void handleFields(ClsDef p, EnvTypecheck env) {
        ClassType c = env.lookupClass(p.ident_);

        for (Member m : p.listmember_) {
            if (m instanceof FnMember) {
                addMethod(c, (FnDef) ((FnMember) m).funcdef_);
            } else if (m instanceof AttrMember) {
                addAttribute(c, (AttrMember) m);
            } else {
                throw new IllegalArgumentException(
                    "Unknown member type: " + m.getClass().getName()
                );
            }
        }
    }

    public Void visit(ClsDef p, EnvTypecheck env) {
        if (constructorOnly) {
            handleConstructor(p, env);
        } else {
            handleFields(p, env);
        }

        return null;
    }
}
