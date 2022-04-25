package fr.rthd.jlc.compiler;

import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.Stmt;
import javalette.Absyn.Void;

class BlkVisitor implements Blk.Visitor<Void, EnvCompiler> {
    public Void visit(Block p, EnvCompiler env) {
        env.emit(env.instructionBuilder.comment("start block"));
        env.indent();

        env.enterScope();
        for (Stmt s : p.liststmt_) {
            s.accept(new StmtVisitor(), env);
        }
        env.leaveScope();

        env.unindent();
        env.emit(env.instructionBuilder.comment("end block"));
        return null;
    }
}
