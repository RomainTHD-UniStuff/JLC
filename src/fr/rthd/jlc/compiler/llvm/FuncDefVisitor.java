package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import javalette.Absyn.Init;

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
            func.addArgFirst(new FunArg(TypeCode.forClass(c.name), "this"));
        }

        func.getArgs().forEach(arg -> {
            Variable var = env.createVar(
                arg.type,
                arg.name,
                !arg.type.isPrimitive() // Primitive types are passed by value
            );
            env.insertVar(arg.name, var);
            arg.setGeneratedName(var.name);
        });

        env.emit(env.instructionBuilder.functionDeclarationStart(c, func));
        env.emit(env.instructionBuilder.label("entry"));

        for (FunArg arg : func.getArgs()) {
            Variable v = env.lookupVar(arg.name);
            if (!v.isPointer()) {
                // Arguments are passed by value, so we load them to respect the
                //  convention that all variables are pointers
                new Init(
                    arg.name,
                    new EVar(arg.name)
                ).accept(new ItemVisitor(
                    arg.type,
                    true
                ), env);
            }
        }

        p.blk_.accept(new BlkVisitor(), env);

        if (func.retType == TypeCode.CVoid) {
            env.emit(env.instructionBuilder.ret());
        } else {
            env.emit(env.instructionBuilder.ret(new Literal(
                func.retType,
                func.retType.getDefaultValue()
            )));
        }

        env.emit(env.instructionBuilder.functionDeclarationEnd());
        env.emit(env.instructionBuilder.newLine());

        return null;
    }
}
