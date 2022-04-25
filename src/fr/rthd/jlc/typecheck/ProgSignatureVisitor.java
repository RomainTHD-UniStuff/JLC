package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.CyclicInheritanceException;
import fr.rthd.jlc.typecheck.exception.InvalidArgumentCountException;
import fr.rthd.jlc.typecheck.exception.InvalidReturnedTypeException;
import fr.rthd.jlc.typecheck.exception.NoSuchClassException;
import fr.rthd.jlc.typecheck.exception.NoSuchFunctionException;
import fr.rthd.jlc.utils.Choice;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;

class ProgSignatureVisitor implements Prog.Visitor<Void, EnvTypecheck> {
    public Void visit(Program p, EnvTypecheck env) {
        for (TopDef def : p.listtopdef_) {
            def.accept(new TopDefClassDefSignatureVisitor(), env);
        }

        for (ClassType c : env.getAllClass()) {
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

        for (TopDef def : p.listtopdef_) {
            def.accept(new TopDefSignatureVisitor(), env);
        }

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

        if (!mainFunc.args.isEmpty()) {
            throw new InvalidArgumentCountException(
                "main",
                0,
                mainFunc.args.size()
            );
        }

        return null;
    }
}
