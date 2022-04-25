package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;
import javalette.Absyn.Void;

class ProgVisitor implements Prog.Visitor<Void, EnvCompiler> {
    public Void visit(Program p, EnvCompiler env) {
        env.emit(env.instructionBuilder.newLine());

        for (FunType fun : env.getAllFun()) {
            if (fun.isExternal()) {
                env.emit(env.instructionBuilder.declareExternalFunction(fun));
            }
        }

        env.emit(env.instructionBuilder.newLine());

        for (TopDef topdef : p.listtopdef_) {
            topdef.accept(new TopDefVisitor(), env);
        }
        return null;
    }
}
