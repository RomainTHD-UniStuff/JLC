package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.internal.NotImplementedException;
import javalette.Absyn.EAdd;
import javalette.Absyn.EAnd;
import javalette.Absyn.EApp;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EMul;
import javalette.Absyn.ENew;
import javalette.Absyn.ENull;
import javalette.Absyn.EOr;
import javalette.Absyn.ERel;
import javalette.Absyn.EString;
import javalette.Absyn.EVar;
import javalette.Absyn.Expr;
import javalette.Absyn.Neg;
import javalette.Absyn.Not;

class ExprVisitor implements Expr.Visitor<AnnotatedExpr<? extends Expr>, EnvOptimizer> {
    public AnnotatedExpr<?> visit(ENull e, EnvOptimizer env) {
        throw new NotImplementedException();
    }

    public AnnotatedExpr<?> visit(EVar e, EnvOptimizer env) {
        /*
        AnnotatedExpr<?> expr = env.lookupVar(e.ident_);
        assert expr != null;
        if (env.constantPropagationEnabled()) {
            return expr;
        } else {
            return new AnnotatedExpr<>(
                expr.getType(),
                new EVar(e.ident_)
            );
        }
         */
        // TODO:
        throw new NotImplementedException();
    }

    public AnnotatedExpr<ELitInt> visit(ELitInt e, EnvOptimizer env) {
        return new AnnotatedExpr<>(TypeCode.CInt, e);
    }

    public AnnotatedExpr<ELitDoub> visit(ELitDoub e, EnvOptimizer env) {
        return new AnnotatedExpr<>(TypeCode.CDouble, e);
    }

    public AnnotatedExpr<ELitTrue> visit(ELitTrue e, EnvOptimizer env) {
        return new AnnotatedExpr<>(TypeCode.CBool, e);
    }

    public AnnotatedExpr<ELitFalse> visit(ELitFalse e, EnvOptimizer env) {
        return new AnnotatedExpr<>(TypeCode.CBool, e);
    }

    public AnnotatedExpr<EApp> visit(EApp e, EnvOptimizer env) {
        /*
        FunTypeOptimizer funcType = env.lookupFun(e.ident_);
        assert funcType != null;

        FunTypeOptimizer currentFunction = env.getCurrentFunction();
        assert currentFunction != null;
        funcType.addUsageIn(currentFunction);

        ListExpr exps = new ListExpr();
        for (int i = 0; i < funcType.getArgs().size(); ++i) {
            AnnotatedExpr<?> exp = e.listexpr_.get(i).accept(
                new ExprVisitor(),
                env
            );
            exps.add(exp);
        }

        return new AnnotatedExpr<>(
            funcType.getRetType(),
            new EApp(e.ident_, exps)
        );*/
        // TODO:
        throw new NotImplementedException();
    }

    public AnnotatedExpr<EString> visit(EString e, EnvOptimizer env) {
        return new AnnotatedExpr<>(TypeCode.CString, e);
    }

    public AnnotatedExpr<? extends Expr> visit(ENew p, EnvOptimizer env) {
        throw new NotImplementedException();
    }

    public AnnotatedExpr<?> visit(Neg e, EnvOptimizer env) {
        AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
        if (expr.getParentExp() instanceof ELitInt) {
            return new AnnotatedExpr<>(
                TypeCode.CInt,
                new ELitInt(-((ELitInt) expr.getParentExp()).integer_)
            );
        } else if (expr.getParentExp() instanceof ELitDoub) {
            return new AnnotatedExpr<>(
                TypeCode.CDouble,
                new ELitDoub(-((ELitDoub) expr.getParentExp()).double_)
            );
        } else {
            return new AnnotatedExpr<>(
                expr.getType(),
                new Neg(expr.getParentExp())
            );
        }
    }

    public AnnotatedExpr<?> visit(Not e, EnvOptimizer env) {
        AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
        if (expr.getParentExp() instanceof ELitTrue) {
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new ELitFalse()
            );
        } else if (expr.getParentExp() instanceof ELitFalse) {
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new ELitTrue()
            );
        } else {
            return new AnnotatedExpr<>(
                expr.getType(),
                new Not(expr.getParentExp())
            );
        }
    }

    public AnnotatedExpr<?> visit(EMul e, EnvOptimizer env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
        return e.mulop_.accept(new MulOpVisitor(left, right), env);
    }

    public AnnotatedExpr<?> visit(EAdd e, EnvOptimizer env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
        return e.addop_.accept(new AddOpVisitor(left, right), env);
    }

    public AnnotatedExpr<?> visit(ERel e, EnvOptimizer env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
        return e.relop_.accept(new RelOpVisitor(left, right), env);
    }

    public AnnotatedExpr<?> visit(EAnd e, EnvOptimizer env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

        if (left.getParentExp() instanceof ELitTrue) {
            return new AnnotatedExpr<>(TypeCode.CBool, right);
        } else if (right.getParentExp() instanceof ELitTrue) {
            return new AnnotatedExpr<>(TypeCode.CBool, left);
        } else if (left.getParentExp() instanceof ELitFalse) {
            // Short circuit
            return new AnnotatedExpr<>(TypeCode.CBool, new ELitFalse());
        } else if (right.getParentExp() instanceof ELitFalse) {
            // Still need to execute the left expression, even though
            // we know it will result in literal false
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EAnd(left, right)
            );
        } else {
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EAnd(left, right)
            );
        }
    }

    public AnnotatedExpr<?> visit(EOr e, EnvOptimizer env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

        if (left.getParentExp() instanceof ELitTrue) {
            // Short circuit
            return new AnnotatedExpr<>(TypeCode.CBool, new ELitTrue());
        } else if (right.getParentExp() instanceof ELitTrue) {
            // Still need to execute the left expression, even though
            // we know it will result in literal true
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EOr(left, right)
            );
        } else if (left.getParentExp() instanceof ELitFalse) {
            return new AnnotatedExpr<>(TypeCode.CBool, right);
        } else if (right.getParentExp() instanceof ELitFalse) {
            return new AnnotatedExpr<>(TypeCode.CBool, left);
        } else {
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EOr(left, right)
            );
        }
    }
}
