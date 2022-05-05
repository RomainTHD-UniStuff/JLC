package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.internal.NotImplementedException;
import fr.rthd.jlc.utils.Choice;
import javalette.Absyn.AddOp;
import javalette.Absyn.Ass;
import javalette.Absyn.BStmt;
import javalette.Absyn.Block;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.EAdd;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EVar;
import javalette.Absyn.Empty;
import javalette.Absyn.For;
import javalette.Absyn.Incr;
import javalette.Absyn.Item;
import javalette.Absyn.ListItem;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;
import javalette.Absyn.Ret;
import javalette.Absyn.SExp;
import javalette.Absyn.Stmt;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

import static fr.rthd.jlc.optimizer.Optimizer.isLiteral;

class StmtVisitor implements Stmt.Visitor<AnnotatedStmt<? extends Stmt>, EnvOptimizer> {
    /**
     * Visitor for increments and decrements
     * @param base Base statement
     * @param ident Identifier
     * @param op Operator
     * @param env Environment
     * @param <T> Type of the expression
     * @return Incremented or decremented expression
     */
    private static <T extends Stmt> AnnotatedStmt<T> visitIncrDecr(
        T base,
        String ident,
        AddOp op,
        EnvOptimizer env
    ) {
        AnnotatedExpr<?> exp = env.lookupVar(ident);
        assert exp != null;
        if (isLiteral(exp)) {
            // We can reduce this to something like `x = n + 1`
            AnnotatedExpr<?> newExp = new EAdd(
                exp,
                op,
                new AnnotatedExpr<>(
                    exp.getType(),
                    new ELitInt(1)
                )
            ).accept(new ExprVisitor(), env);

            if (env.isTopLevel(exp)) {
                // We're in the same block, we can just update the value
                env.updateVar(ident, newExp);
            } else {
                // Different block, we need to repudiate the old value, but
                //  still insert the new value for future use in this block
                env.updateVar(ident, new AnnotatedExpr<>(
                    exp.getType(),
                    new EVar(ident)
                ));
                env.insertVar(ident, newExp);
            }
        }
        return new AnnotatedStmt<>(base);
    }

    public AnnotatedStmt<Empty> visit(Empty s, EnvOptimizer env) {
        return new AnnotatedStmt<>(new Empty());
    }

    public AnnotatedStmt<Stmt> visit(BStmt s, EnvOptimizer env) {
        Block blk = (Block) s.blk_.accept(new BlkVisitor(), env);
        if (blk.liststmt_.size() == 0) {
            return new AnnotatedStmt<>(new Empty());
        }

        // Not empty because of above, and shouldn't contain a returning
        //  statement if the last one isn't a returning statement as well,
        //  otherwise it means we didn't do a clean cut
        boolean doesReturn = (
            (AnnotatedStmt<?>) blk.liststmt_.get(blk.liststmt_.size() - 1)
        ).doesReturn();

        return new AnnotatedStmt<>(new BStmt(blk), doesReturn);
    }

    public AnnotatedStmt<Decl> visit(Decl s, EnvOptimizer env) {
        TypeCode type = s.type_.accept(new TypeVisitor(), null);
        ListItem items = new ListItem();

        for (Item item : s.listitem_) {
            items.add(item.accept(new ItemVisitor(type), env));
        }

        return new AnnotatedStmt<>(new Decl(s.type_, items));
    }

    public AnnotatedStmt<Ass> visit(Ass s, EnvOptimizer env) {
        AnnotatedExpr<?> exp = s.expr_2.accept(new ExprVisitor(), env);
        String varName = ((EVar) s.expr_1).ident_;
        if (isLiteral(exp)) {
            // We can reduce this to something like `x = n`
            if (env.isTopLevel(env.lookupVar(varName))) {
                // We're in the same block, we can just update the value
                env.updateVar(varName, exp);
            } else {
                // Different block, we need to repudiate the old value, but
                //  still insert the new value for future use in this block
                env.updateVar(varName, new AnnotatedExpr<>(
                    exp.getType(),
                    new EVar(varName)
                ));
                env.insertVar(varName, exp);
            }
        } else {
            // Not a literal, we lose the ability to optimize this variable
            env.updateVar(varName, new AnnotatedExpr<>(
                exp.getType(),
                new EVar(varName)
            ));
        }

        return new AnnotatedStmt<>(new Ass(s.expr_1, exp));
    }

    public AnnotatedStmt<Incr> visit(Incr s, EnvOptimizer env) {
        return visitIncrDecr(s, s.ident_, new Plus(), env);
    }

    public AnnotatedStmt<Decr> visit(Decr s, EnvOptimizer env) {
        return visitIncrDecr(s, s.ident_, new Minus(), env);
    }

    public AnnotatedStmt<Ret> visit(Ret s, EnvOptimizer env) {
        return new AnnotatedStmt<>(new Ret(s.expr_.accept(
            new ExprVisitor(),
            env
        )), true);
    }

    public AnnotatedStmt<VRet> visit(VRet s, EnvOptimizer env) {
        return new AnnotatedStmt<>(new VRet(), true);
    }

    public AnnotatedStmt<?> visit(Cond s, EnvOptimizer env) {
        AnnotatedExpr<?> exp = s.expr_.accept(
            new ExprVisitor(),
            env
        );

        if (exp.getParentExp() instanceof ELitTrue) {
            return s.stmt_.accept(new StmtVisitor(), env);
        } else if (exp.getParentExp() instanceof ELitFalse) {
            return new AnnotatedStmt<>(new Empty());
        } else {
            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();

            return new AnnotatedStmt<>(new Cond(exp, stmt));
        }
    }

    public AnnotatedStmt<?> visit(CondElse s, EnvOptimizer env) {
        AnnotatedExpr<?> exp = s.expr_.accept(
            new ExprVisitor(),
            env
        );

        if (exp.getParentExp() instanceof ELitTrue) {
            return s.stmt_1.accept(new StmtVisitor(), env);
        } else if (exp.getParentExp() instanceof ELitFalse) {
            return s.stmt_2.accept(new StmtVisitor(), env);
        } else {
            env.enterScope();
            AnnotatedStmt<?> stmt1 = s.stmt_1.accept(
                new StmtVisitor(),
                env
            );
            env.leaveScope();

            env.enterScope();
            AnnotatedStmt<?> stmt2 = s.stmt_2.accept(
                new StmtVisitor(),
                env
            );
            env.leaveScope();

            return new AnnotatedStmt<>(
                new CondElse(exp, stmt1, stmt2),
                stmt1.doesReturn() && stmt2.doesReturn()
            );
        }
    }

    public AnnotatedStmt<?> visit(While s, EnvOptimizer env) {
        // We need to disable constant propagation for the condition,
        //  otherwise something like `while (x != 0) x--;` will never be
        //  executed. On the other hand, we cannot evaluate the body before
        //  the condition, because the condition might evaluate to false
        //  and the body will prevent some possible optimizations about
        //  purity, unused functions etc. This solution is a good compromise
        //  even though we might be able to optimize the condition more,
        //  especially if the body never updates the variable used in the
        //  condition.
        //  TODO: We might be able to optimize the condition more

        env.setConstantPropagation(false);
        AnnotatedExpr<?> exp = s.expr_.accept(
            new ExprVisitor(),
            env
        );
        env.setConstantPropagation(true);

        // TODO: Optimize infinite loop

        if (exp.getParentExp() instanceof ELitFalse) {
            return new AnnotatedStmt<>(new Empty());
        } else {
            // FIXME: Constant propagation in while body must be fixed
            env.setConstantPropagation(false);
            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();
            env.setConstantPropagation(true);

            if (exp.getParentExp() instanceof ELitTrue) {
                // Functions with infinite loops cannot safely be marked
                //  as pure for the sake of optimization, so we can simply
                //  remove functions calls that are known to be pure
                FunTypeOptimizer currentFunction = env.getCurrentFunction();
                assert currentFunction != null;
                currentFunction.setPure(Choice.FALSE);
            }

            return new AnnotatedStmt<>(
                new While(exp, stmt),
                exp.getParentExp() instanceof ELitTrue
            );
        }
    }

    public AnnotatedStmt<?> visit(For p, EnvOptimizer env) {
        throw new NotImplementedException();
    }

    public AnnotatedStmt<?> visit(SExp s, EnvOptimizer env) {
        /*
        AnnotatedExpr<?> expr = (AnnotatedExpr<?>) s.expr_;
        if (expr.getParentExp() instanceof EApp) {
            FunTypeOptimizer funType = env.lookupFun(
                ((EApp) expr.getParentExp()).ident_
            );
            assert funType != null;
            if (funType.isPure() == Choice.TRUE) {
                return new AnnotatedStmt<>(new Empty());
            }
        }
        expr = s.expr_.accept(
            new ExprVisitor(),
            env
        );
        return new AnnotatedStmt<>(new SExp(expr));
        */
        // TODO:
        throw new NotImplementedException();
    }
}
