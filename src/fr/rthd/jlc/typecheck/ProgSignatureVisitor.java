package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.CyclicInheritanceException;
import fr.rthd.jlc.typecheck.exception.InvalidArgumentCountException;
import fr.rthd.jlc.typecheck.exception.InvalidReturnedTypeException;
import fr.rthd.jlc.typecheck.exception.NoSuchClassException;
import fr.rthd.jlc.typecheck.exception.NoSuchFunctionException;
import fr.rthd.jlc.utils.Choice;
import javalette.Absyn.Argument;
import javalette.Absyn.Ass;
import javalette.Absyn.Block;
import javalette.Absyn.Class;
import javalette.Absyn.FnDef;
import javalette.Absyn.ListArg;
import javalette.Absyn.ListStmt;
import javalette.Absyn.ListTopDef;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class ProgSignatureVisitor implements Prog.Visitor<Prog, EnvTypecheck> {
    public Prog visit(Program p, EnvTypecheck env) {
        ListTopDef listTopDef = p.listtopdef_;

        for (TopDef def : listTopDef) {
            // List all the classes
            def.accept(new TopDefClassDefSignatureVisitor(), env);
        }

        for (ClassType c : env.getAllClass()) {
            // Update all the classes' superclass with a java object reference
            if (c.superclassName == null) {
                c.updateSuperclass(null);
            } else {
                ClassType superclass = env.lookupClass(c.superclassName);
                if (superclass == null) {
                    throw new NoSuchClassException(c.superclassName);
                }
                c.updateSuperclass(superclass);
            }
        }

        for (ClassType c : env.getAllClass()) {
            // Check for cyclic inheritance
            ClassType superclass = c;
            do {
                if (c.equals(superclass.getSuperclass())) {
                    throw new CyclicInheritanceException(
                        superclass.name,
                        c.name
                    );
                }
                superclass = superclass.getSuperclass();
            } while (superclass != null);
        }

        for (TopDef def : listTopDef) {
            // List all the functions, methods and attributes
            def.accept(new TopDefSignatureVisitor(), env);
        }

        for (ClassType c : env.getAllClass()) {
            // Add the constructors

            ListArg args = new ListArg();
            args.add(new Argument(new Class(c.name), "this"));

            ListStmt body = new ListStmt();
            for (Attribute attr : c.getAllAttributes()) {
                if (attr.type.isPrimitive()) {
                    // FIXME: The constructor shouldn't be a top-level function
                    //  but a method of the class, so that it can access the
                    //  attributes directly.
                    body.add(new Ass(
                        attr.name,
                        AnnotatedExpr.getDefaultValue(attr.type)
                    ));
                }
            }

            TopDef def = new TopFnDef(
                new FnDef(
                    new javalette.Absyn.Void(),
                    c.getConstructorName(),
                    args,
                    new Block(body)
                )
            );

            def.accept(new TopDefSignatureVisitor(), env);
            listTopDef.add(def);
        }

        // Add the external functions like `printInt`

        env.insertFun(new FunType(
            TypeCode.CVoid,
            "printInt",
            new FunArg(TypeCode.CInt, "i")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CVoid,
            "printDouble",
            new FunArg(TypeCode.CDouble, "d")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CVoid,
            "printString",
            new FunArg(TypeCode.CString, "s")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CInt,
            "readInt"
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CDouble,
            "readDouble"
        ).setExternal().setPure(Choice.FALSE));

        // Check for the main function

        FunType mainFunc = env.lookupFun("main");
        if (mainFunc == null) {
            throw new NoSuchFunctionException("main");
        } else {
            mainFunc.setAsMain();
        }

        if (mainFunc.retType != TypeCode.CInt) {
            throw new InvalidReturnedTypeException(
                "main",
                TypeCode.CInt,
                mainFunc.retType
            );
        }

        if (!mainFunc.getArgs().isEmpty()) {
            throw new InvalidArgumentCountException(
                "main",
                0,
                mainFunc.getArgs().size()
            );
        }

        return new Program(listTopDef);
    }
}
