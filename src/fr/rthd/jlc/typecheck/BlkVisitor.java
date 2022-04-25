package fr.rthd.jlc.typecheck;

import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.ListStmt;
import javalette.Absyn.Stmt;

class BlkVisitor implements Blk.Visitor<Blk, EnvTypecheck> {
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
