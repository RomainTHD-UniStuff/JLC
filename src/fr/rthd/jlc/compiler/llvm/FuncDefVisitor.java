package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import javalette.Absyn.Init;

import java.util.List;

class FuncDefVisitor implements FuncDef.Visitor<Void, EnvCompiler> {
    public Void visit(FnDef p, EnvCompiler env) {
        FunType func;

        ClassType c = env.getCurrentClass();
        if (c == null) {
            func = env.lookupFun(p.ident_);
        } else {
            func = c.getMethod(p.ident_);
        }

        env.resetScope();

        if (c != null) {
            // `this` is the first argument
            func.addArgFirst(new FunArg(c.getType(), "this"));
        }

        func.getArgs().forEach(arg -> {
            Variable var = env.createVar(
                arg.getType(),
                arg.getName(),
                !arg.getType().isPrimitive()// Primitive types are passed by value
            );
            env.insertVar(arg.getName(), var);
            arg.setGeneratedName(var.getName());
        });

        env.emit(env.instructionBuilder.functionDeclarationStart(c, func));
        env.emit(env.instructionBuilder.label("entry"));

        for (FunArg arg : func.getArgs()) {
            Variable v = env.lookupVar(arg.getName());
            if (!v.isPointer()) {
                // Arguments are passed by value, so we load them to respect the
                //  convention that all variables are pointers
                new Init(
                    arg.getName(),
                    new EVar(arg.getName())
                ).accept(new ItemVisitor(
                    arg.getType(),
                    true
                ), env);
            }
        }

        if (c != null) {
            List<Attribute> attrs = c.getAllAttributes();
            for (int i = 0; i < attrs.size(); i++) {
                Attribute a = attrs.get(i);
                Variable v = env.createVar(
                    a.getType(),
                    a.getName(),
                    true,
                    true
                );
                env.insertVar(a.getName(), v);
                env.emit(env.instructionBuilder.loadAttribute(
                    v,
                    env.lookupVar("this"),
                    i
                ));
            }
        }

        p.blk_.accept(new BlkVisitor(), env);

        if (func.getRetType() == TypeCode.CVoid) {
            env.emit(env.instructionBuilder.ret());
        } else {
            env.emit(env.instructionBuilder.ret(new Literal(
                func.getRetType(),
                func.getRetType().getDefaultValue()
            )));
        }

        env.emit(env.instructionBuilder.functionDeclarationEnd());
        env.emit(env.instructionBuilder.newLine());

        return null;
    }
}
