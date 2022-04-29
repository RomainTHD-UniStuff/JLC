package fr.rthd.jlc.compiler.llvm;

import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.Stmt;
import javalette.Absyn.Void;
import org.jetbrains.annotations.NonNls;

/**
 * Block visitor
 * @author RomainTHD
 */
@NonNls
class BlkVisitor implements Blk.Visitor<Void, EnvCompiler> {
    /**
     * Visit a block
     * @param p Block
     * @param env Environment
     */
    @Override
    public Void visit(Block p, EnvCompiler env) {
        env.emit(env.instructionBuilder.comment("start block"));
        env.indent();

        env.enterScope();
        for (Stmt s : p.liststmt_) {
            // Accept all statements
            s.accept(new StmtVisitor(), env);
        }
        env.leaveScope();

        env.unindent();
        env.emit(env.instructionBuilder.comment("end block"));
        return null;
    }
}
