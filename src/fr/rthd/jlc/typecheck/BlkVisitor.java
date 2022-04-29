package fr.rthd.jlc.typecheck;

import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.ListStmt;
import javalette.Absyn.Stmt;
import org.jetbrains.annotations.NonNls;

/**
 * Block visitor
 * @author RomainTHD
 */
@NonNls
class BlkVisitor implements Blk.Visitor<Blk, EnvTypecheck> {
    /**
     * Visit a block
     * @param p Block
     * @param env Environment
     * @return Block
     */
    @Override
    public Block visit(Block p, EnvTypecheck env) {
        ListStmt statements = new ListStmt();

        env.enterScope();

        for (Stmt s : p.liststmt_) {
            statements.add(s.accept(new StmtVisitor(), env));
        }

        env.leaveScope();

        return new Block(statements);
    }
}
