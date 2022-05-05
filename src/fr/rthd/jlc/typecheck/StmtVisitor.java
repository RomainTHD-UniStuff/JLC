package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
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
import javalette.Absyn.EVar;
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
import org.jetbrains.annotations.NonNls;

/**
 * Statement visitor. All the expressions will be replaced by an annotated
 * expression with its type and eventually additional information
 * @author RomainTHD
 * @see AnnotatedExpr
 */
@NonNls
class StmtVisitor implements Stmt.Visitor<Stmt, EnvTypecheck> {
    /**
     * Empty statement
     * @param s Empty statement
     * @param env Environment
     * @return Empty statement
     */
    @Override
    public Empty visit(Empty s, EnvTypecheck env) {
        return new Empty();
    }

    /**
     * Block
     * @param s Block
     * @param env Environment
     * @return Block
     */
    @Override
    public BStmt visit(BStmt s, EnvTypecheck env) {
        return new BStmt(s.blk_.accept(new BlkVisitor(), env));
    }

    /**
     * Declaration
     * @param s Declaration
     * @param env Environment
     * @return Declaration
     */
    @Override
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

    /**
     * Assignment
     * @param s Assignment
     * @param env Environment
     * @return Assignment
     */
    @Override
    public Ass visit(Ass s, EnvTypecheck env) {
        String v;

        if (s.expr_1 instanceof EVar) {
            v = ((EVar) s.expr_1).ident_;
        } else {
            throw new NotImplementedException();
        }

        // FIXME: Check that there are no conflicts with `.length` or a method

        TypeCode expectedType = env.lookupVar(v);
        if (expectedType == null) {
            throw new NoSuchVariableException(v);
        }

        AnnotatedExpr<?> exp = s.expr_2.accept(new ExprVisitor(), env);
        TypeException e = new InvalidAssignmentTypeException(
            v,
            expectedType,
            exp.getType()
        );

        if (exp.getType().isObject()) {
            if (!expectedType.isObject()) {
                // `int x = new A;`
                throw e;
            }

            ClassType expectedClass = env.lookupClass(expectedType);
            assert expectedClass != null;
            ClassType actualClass = env.lookupClass(exp.getType());
            assert actualClass != null;
            if (!actualClass.isSubclassOf(expectedClass)) {
                // `B x = new A;`
                throw e;
            }
        } else if (exp.getType() != expectedType) {
            // `int x = true;`
            throw e;
        }

        // FIXME: Use annotated expression for left member?
        return new Ass(s.expr_1, exp);
    }

    /**
     * Increment
     * @param s Increment
     * @param env Environment
     * @return Increment
     */
    @Override
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

    /**
     * Decrement
     * @param s Decrement
     * @param env Environment
     * @return Decrement
     */
    @Override
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

    /**
     * Return with value
     * @param s Return with value
     * @param env Environment
     * @return Return with value
     */
    @Override
    public Ret visit(Ret s, EnvTypecheck env) {
        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        FunType f = env.getCurrentFunction();
        assert f != null;
        if (exp.getType() != f.getRetType()) {
            throw new InvalidReturnedTypeException(
                env.getCurrentFunction().getRetType(),
                exp.getType()
            );
        }

        env.setReturn(true);
        return new Ret(exp);
    }

    /**
     * Return without value
     * @param s Return without value
     * @param env Environment
     * @return Return without value
     */
    @Override
    public VRet visit(VRet s, EnvTypecheck env) {
        FunType f = env.getCurrentFunction();
        assert f != null;
        if (f.getRetType() != TypeCode.CVoid) {
            throw new InvalidReturnedTypeException(
                env.getCurrentFunction().getRetType(),
                TypeCode.CVoid
            );
        }

        env.setReturn(true);
        return new VRet();
    }

    /**
     * If
     * @param s If
     * @param env Environment
     * @return If
     */
    @Override
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

    /**
     * If-else
     * @param s If-else
     * @param env Environment
     * @return If-else
     */
    @Override
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

    /**
     * While
     * @param s While
     * @param env Environment
     * @return While
     */
    @Override
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

    /**
     * For
     * @param s For
     * @param env Environment
     * @return For
     */
    @Override
    public For visit(For s, EnvTypecheck env) {
        throw new NotImplementedException();
    }

    /**
     * Statement expression, like `f();`
     * @param s Statement expression
     * @param env Environment
     * @return Statement expression
     */
    @Override
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
