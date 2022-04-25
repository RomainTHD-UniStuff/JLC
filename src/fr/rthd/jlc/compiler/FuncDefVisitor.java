package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import javalette.Absyn.Init;
import javalette.Absyn.Void;

class FuncDefVisitor implements FuncDef.Visitor<Void, EnvCompiler> {
    public Void visit(FnDef p, EnvCompiler env) {
        FunType func = env.lookupFun(p.ident_);

        env.resetScope();

        func.args.forEach(arg -> {
            Variable var = env.createVar(arg.type, arg.name, false);
            env.insertVar(arg.name, var);
            arg.setGeneratedName(var.name);
        });

        env.emit(env.instructionBuilder.functionDeclarationStart(func));
        env.emit(env.instructionBuilder.label("entry"));

        func.args.forEach(arg -> new Init(
            arg.name,
            new EVar(arg.name)
        ).accept(new ItemVisitor(
            arg.type,
            true
        ), env));

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
