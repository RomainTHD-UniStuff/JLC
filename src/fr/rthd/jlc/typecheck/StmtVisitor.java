package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.internal.NotImplementedException;
import fr.rthd.jlc.typecheck.exception.InvalidAssignmentTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidConditionTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidDeclaredTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidExpressionTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidOperationException;
import fr.rthd.jlc.typecheck.exception.InvalidReturnedTypeException;
import fr.rthd.jlc.typecheck.exception.NoSuchClassException;
import fr.rthd.jlc.typecheck.exception.NoSuchVariableException;
import fr.rthd.jlc.typecheck.exception.TypeException;
import javalette.Absyn.Ass;
import javalette.Absyn.BStmt;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.Empty;
import javalette.Absyn.For;
import javalette.Absyn.Incr;
import javalette.Absyn.Item;
import javalette.Absyn.ListItem;
import javalette.Absyn.Ret;
import javalette.Absyn.SExp;
import javalette.Absyn.Stmt;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

class StmtVisitor implements Stmt.Visitor<Stmt, EnvTypecheck> {
    public Empty visit(Empty s, EnvTypecheck env) {
        return new Empty();
    }

    public BStmt visit(BStmt s, EnvTypecheck env) {
        return new BStmt(s.blk_.accept(new BlkVisitor(), env));
    }

    public Decl visit(Decl s, EnvTypecheck env) {
        TypeCode type = s.type_.accept(new TypeVisitor(), null);
        if (type == TypeCode.CVoid) {
            throw new InvalidDeclaredTypeException(
                type
            );
        }

        if (type.isObject() && env.lookupClass(type) == null) {
            throw new NoSuchClassException(type);
        }

        ListItem items = new ListItem();

        for (Item item : s.listitem_) {
            items.add(item.accept(new ItemVisitor(type), env));
        }

        return new Decl(s.type_, items);
    }

    public Ass visit(Ass s, EnvTypecheck env) {
        TypeCode expectedType = env.lookupVar(s.ident_);
        if (expectedType == null) {
            throw new NoSuchVariableException(s.ident_);
        }

        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        TypeException e = new InvalidAssignmentTypeException(
            s.ident_,
            expectedType,
            exp.getType()
        );

        if (exp.getType().isObject()) {
            if (!expectedType.isObject()) {
                // `int x = new A;`
                throw e;
            }

            ClassType expectedClass = env.lookupClass(expectedType);
            ClassType actualClass = env.lookupClass(exp.getType());
            if (!actualClass.isCastableTo(expectedClass)) {
                // `B x = new A;`
                throw e;
            }
        } else if (exp.getType() != expectedType) {
            // `int x = true;`
            throw e;
        }

        return new Ass(s.ident_, exp);
    }

    public Incr visit(Incr s, EnvTypecheck env) {
        TypeCode varType = env.lookupVar(s.ident_);

        if (varType == null) {
            throw new NoSuchVariableException(s.ident_);
        }

        if (varType != TypeCode.CInt) {
            throw new InvalidOperationException(
                "increment",
                varType,
                TypeCode.CInt
            );
        }

        return new Incr(s.ident_);
    }

    public Decr visit(Decr s, EnvTypecheck env) {
        TypeCode varType = env.lookupVar(s.ident_);

        if (varType == null) {
            throw new NoSuchVariableException(s.ident_);
        }

        if (varType != TypeCode.CInt) {
            throw new InvalidOperationException(
                "decrement",
                varType,
                TypeCode.CInt
            );
        }

        return new Decr(s.ident_);
    }

    public Ret visit(Ret s, EnvTypecheck env) {
        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        if (exp.getType() != env.getCurrentFunction().getRetType()) {
            throw new InvalidReturnedTypeException(
                env.getCurrentFunction().getRetType(),
                exp.getType()
            );
        }

        env.setReturn(true);
        return new Ret(exp);
    }

    public VRet visit(VRet s, EnvTypecheck env) {
        if (env.getCurrentFunction().getRetType() != TypeCode.CVoid) {
            throw new InvalidReturnedTypeException(
                env.getCurrentFunction().getRetType(),
                TypeCode.CVoid
            );
        }

        env.setReturn(true);
        return new VRet();
    }

    public Cond visit(Cond s, EnvTypecheck env) {
        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        if (exp.getType() != TypeCode.CBool) {
            throw new InvalidConditionTypeException("if", exp.getType());
        }

        boolean doesReturn = env.doesReturn();

        env.enterScope();
        Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
        env.leaveScope();

        env.setReturn(doesReturn);

        return new Cond(exp, stmt);
    }

    public CondElse visit(CondElse s, EnvTypecheck env) {
        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        if (exp.getType() != TypeCode.CBool) {
            throw new InvalidConditionTypeException("if-else", exp.getType());
        }

        boolean doesReturn = env.doesReturn();

        env.enterScope();
        Stmt stmt1 = s.stmt_1.accept(new StmtVisitor(), env);
        env.leaveScope();

        boolean doesReturnIf = env.doesReturn();
        env.setReturn(doesReturn);

        env.enterScope();
        Stmt stmt2 = s.stmt_2.accept(new StmtVisitor(), env);
        env.leaveScope();

        boolean doesReturnElse = env.doesReturn();

        env.setReturn(doesReturn || (doesReturnIf && doesReturnElse));

        return new CondElse(exp, stmt1, stmt2);
    }

    public While visit(While s, EnvTypecheck env) {
        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        if (exp.getType() != TypeCode.CBool) {
            throw new InvalidConditionTypeException("while", exp.getType());
        }

        boolean doesReturn = env.doesReturn();

        env.enterScope();
        Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
        env.leaveScope();

        env.setReturn(doesReturn);

        return new While(exp, stmt);
    }

    public For visit(For p, EnvTypecheck env) {
        throw new NotImplementedException();
    }

    public SExp visit(SExp s, EnvTypecheck env) {
        AnnotatedExpr<?> expr = s.expr_.accept(new ExprVisitor(), env);

        if (expr.getType() != TypeCode.CVoid) {
            throw new InvalidExpressionTypeException(
                expr.getType(),
                TypeCode.CVoid
            );
        }

        return new SExp(expr);
    }
}
