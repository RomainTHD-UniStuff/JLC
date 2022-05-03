package fr.rthd.jlc.optimizer;

import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.Empty;
import javalette.Absyn.ListStmt;
import javalette.Absyn.Stmt;

class BlkVisitor implements Blk.Visitor<Blk, EnvOptimizer> {
    public Block visit(Block p, EnvOptimizer env) {
        ListStmt statements = new ListStmt();

        env.enterScope();

        for (Stmt s : p.liststmt_) {
            AnnotatedStmt<?> stmt = s.accept(new StmtVisitor(), env);
            if (stmt.getParentStmt() instanceof Empty) {
                continue;
            }

            statements.add(stmt);

            if (stmt.doesReturn()) {
                break;
            }
        }

        env.leaveScope();

        return new Block(statements);
    }
}
