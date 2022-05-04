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
import javalette.Absyn.LValueV;
import javalette.Absyn.ListArg;
import javalette.Absyn.ListIndex;
import javalette.Absyn.ListStmt;
import javalette.Absyn.Member;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * We need to visit the class definition twice: first we list all the classes,
 * then we fill them with their attributes and methods (here). This is because a
 * function returning an object could see this object not recognized initially
 * @author RomainTHD
 * @see ClassDefOnlySignatureVisitor
 */
@NonNls
class ClassDefSignatureVisitor implements ClassDef.Visitor<Void, EnvTypecheck> {
    /**
     * Only add the constructor to the class
     */
    private final boolean constructorOnly;

    /**
     * Constructor
     * @param constructorOnly Constructor only
     */
    public ClassDefSignatureVisitor(boolean constructorOnly) {
        this.constructorOnly = constructorOnly;
    }

    /**
     * Add constructor to the current class. Needs to be done after the fields
     * have been added, hence the second visit
     * @param p Class definition
     * @param env Environment
     */
    private void handleConstructor(
        @NotNull ClsDef p,
        @NotNull EnvTypecheck env
    ) {
        ClassType c = env.lookupClass(p.ident_);
        assert c != null;

        ListStmt body = new ListStmt();
        for (Attribute attr : c.getAllAttributes()) {
            if (attr.getType().isPrimitive()) {
                body.add(new Ass(
                    new LValueV(attr.getName(), new ListIndex()),
                    AnnotatedExpr.getDefaultValue(attr.getType())
                ));
            }
        }

        FnDef fdef = new FnDef(
            new javalette.Absyn.Void(),
            ClassType.CONSTRUCTOR_NAME,
            new ListArg(),
            new Block(body)
        );

        p.listmember_.add(new FnMember(fdef));
        addMethod(c, fdef, true);
    }

    /**
     * Add a method to the current class
     * @param c Class
     * @param f Method
     * @param override Override or not
     */
    private void addMethod(
        @NotNull ClassType c,
        @NotNull FnDef f,
        boolean override
    ) {
        List<FunArg> args = new LinkedList<>();
        for (Arg arg : f.listarg_) {
            args.add(arg.accept(new ArgVisitor(), null));
        }
        if (!override && c.hasMethod(f.ident_)) {
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
        ), override);
    }

    /**
     * Add an attribute to the current class
     * @param c Class
     * @param a Attribute
     */
    private void addAttribute(@NotNull ClassType c, @NotNull AttrMember a) {
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

    /**
     * Handle the field definition of the class
     * @param p Class definition
     * @param env Environment
     */
    private void handleFields(@NotNull ClsDef p, @NotNull EnvTypecheck env) {
        ClassType c = env.lookupClass(p.ident_);
        assert c != null;

        for (Member m : p.listmember_) {
            if (m instanceof FnMember) {
                addMethod(c, (FnDef) ((FnMember) m).funcdef_, false);
            } else if (m instanceof AttrMember) {
                addAttribute(c, (AttrMember) m);
            } else {
                throw new IllegalArgumentException(
                    "Unknown member type: " + m.getClass().getName()
                );
            }
        }
    }

    /**
     * Class definition
     * @param p Class definition
     * @param env Environment
     */
    @Override
    public Void visit(ClsDef p, EnvTypecheck env) {
        if (constructorOnly) {
            handleConstructor(p, env);
        } else {
            handleFields(p, env);
        }

        return null;
    }
}
