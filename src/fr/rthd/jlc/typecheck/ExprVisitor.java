package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.InvalidArgumentCountException;
import fr.rthd.jlc.typecheck.exception.InvalidAssignmentTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidMethodCallException;
import fr.rthd.jlc.typecheck.exception.InvalidNewTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidOperationException;
import fr.rthd.jlc.typecheck.exception.NoSuchClassException;
import fr.rthd.jlc.typecheck.exception.NoSuchFunctionException;
import fr.rthd.jlc.typecheck.exception.NoSuchVariableException;
import fr.rthd.jlc.typecheck.exception.SelfOutOfClassException;
import javalette.Absyn.EAdd;
import javalette.Absyn.EAnd;
import javalette.Absyn.EApp;
import javalette.Absyn.EDot;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EMul;
import javalette.Absyn.ENew;
import javalette.Absyn.ENull;
import javalette.Absyn.EOr;
import javalette.Absyn.ERel;
import javalette.Absyn.ESelf;
import javalette.Absyn.EString;
import javalette.Absyn.EVar;
import javalette.Absyn.Expr;
import javalette.Absyn.ListExpr;
import javalette.Absyn.Mod;
import javalette.Absyn.Neg;
import javalette.Absyn.Not;
import org.jetbrains.annotations.NonNls;

/**
 * Expression visitor, will typecheck an expression and return an annotated
 * expression with the type of the expression
 * @author RomainTHD
 * @see AnnotatedExpr
 */
@NonNls
class ExprVisitor implements Expr.Visitor<AnnotatedExpr<?>, EnvTypecheck> {
    /**
     * Variable
     * @param e Variable
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<EVar> visit(EVar e, EnvTypecheck env) {
        TypeCode varType = env.lookupVar(e.ident_);
        if (varType == null) {
            throw new NoSuchVariableException(e.ident_);
        }

        return new AnnotatedExpr<>(varType, e);
    }

    /**
     * Integer literal
     * @param e Integer literal
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ELitInt> visit(ELitInt e, EnvTypecheck env) {
        return new AnnotatedExpr<>(TypeCode.CInt, e);
    }

    /**
     * Double literal
     * @param e Double literal
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ELitDoub> visit(ELitDoub e, EnvTypecheck env) {
        return new AnnotatedExpr<>(TypeCode.CDouble, e);
    }

    /**
     * Boolean true literal
     * @param e Boolean true literal
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ELitTrue> visit(ELitTrue e, EnvTypecheck env) {
        return new AnnotatedExpr<>(TypeCode.CBool, e);
    }

    /**
     * Boolean false literal
     * @param e Boolean false literal
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ELitFalse> visit(ELitFalse e, EnvTypecheck env) {
        return new AnnotatedExpr<>(TypeCode.CBool, e);
    }

    /**
     * Self reference
     * @param e Self reference
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ESelf> visit(ESelf e, EnvTypecheck env) {
        ClassType c = env.getCurrentClass();
        if (c == null) {
            throw new SelfOutOfClassException();
        }

        return new AnnotatedExpr<>(c.getType(), e);
    }

    /**
     * Function call
     * @param e Function call
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<EApp> visit(EApp e, EnvTypecheck env) {
        FunType funcType = env.lookupFun(e.ident_);
        if (funcType == null) {
            // Might happen for class methods
            ClassType c = env.getCaller();
            if (c != null) {
                funcType = c.getMethod(e.ident_, true);
            }
        }

        if (funcType == null) {
            throw new NoSuchFunctionException(e.ident_);
        }

        if (e.listexpr_.size() != funcType.getArgs().size()) {
            throw new InvalidArgumentCountException(
                e.ident_,
                funcType.getArgs().size(),
                e.listexpr_.size()
            );
        }

        ListExpr exps = new ListExpr();
        for (int i = 0; i < funcType.getArgs().size(); ++i) {
            FunArg expected = funcType.getArgs().get(i);
            AnnotatedExpr<?> exp = e
                .listexpr_
                .get(i)
                .accept(
                    new ExprVisitor(),
                    env
                );
            if (exp.getType() != expected.getType()) {
                throw new InvalidAssignmentTypeException(
                    expected.getName(),
                    expected.getType(),
                    exp.getType(),
                    true
                );
            }
            exps.add(exp);
        }

        return new AnnotatedExpr<>(
            funcType.getRetType(),
            new EApp(e.ident_, exps)
        );
    }

    /**
     * String literal
     * @param e String literal
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<EString> visit(EString e, EnvTypecheck env) {
        return new AnnotatedExpr<>(TypeCode.CString, e);
    }

    /**
     * Method call
     * @param p Method call
     * @param env Environment
     * @return Annotated expression
     * @see ExprVisitor#visit(EApp, EnvTypecheck)
     */
    @Override
    public AnnotatedExpr<EDot> visit(EDot p, EnvTypecheck env) {
        AnnotatedExpr<?> expr = p.expr_.accept(
            new ExprVisitor(),
            env
        );

        if (!expr.getType().isObject()) {
            throw new InvalidMethodCallException(expr.getType());
        }

        ClassType c = env.lookupClass(expr.getType());
        if (c == null) {
            // It should be impossible to get here, since it would mean we
            //  created a variable with a type that doesn't exist
            throw new IllegalStateException("Class not found");
        }

        env.setCaller(c);

        // Method calls and function calls work the same way
        AnnotatedExpr<?> app = new EApp(
            p.ident_,
            p.listexpr_
        ).accept(new ExprVisitor(), env);

        env.setCaller(null);

        return new AnnotatedExpr<>(
            app.getType(),
            new EDot(
                expr,
                p.ident_,
                ((EApp) app.getParentExp()).listexpr_
            )
        );
    }

    /**
     * Null literal
     * @param e Null literal
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ENull> visit(ENull e, EnvTypecheck env) {
        ClassType c = env.lookupClass(e.ident_);
        if (c == null) {
            throw new NoSuchClassException(e.ident_);
        }
        return new AnnotatedExpr<>(c.getType(), e);
    }

    /**
     * New expression
     * @param e Expression
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<ENew> visit(ENew e, EnvTypecheck env) {
        TypeCode t = e.type_.accept(new TypeVisitor(), null);
        if (t.isPrimitive()) {
            throw new InvalidNewTypeException(t);
        }

        if (env.lookupClass(t) == null) {
            throw new NoSuchClassException(t);
        }

        return new AnnotatedExpr<>(t, e);
    }

    /**
     * Mathematical negation
     * @param e Negation
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<Neg> visit(Neg e, EnvTypecheck env) {
        AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
        if (expr.getType() != TypeCode.CInt &&
            expr.getType() != TypeCode.CDouble) {
            throw new InvalidOperationException(
                "negation",
                expr.getType(),
                TypeCode.CInt,
                TypeCode.CDouble
            );
        }

        return new AnnotatedExpr<>(expr.getType(), new Neg(expr));
    }

    /**
     * Boolean negation
     * @param e Negation
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<Not> visit(Not e, EnvTypecheck env) {
        AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
        if (expr.getType() != TypeCode.CBool) {
            throw new InvalidOperationException(
                "not",
                expr.getType(),
                TypeCode.CBool
            );
        }

        return new AnnotatedExpr<>(
            TypeCode.CBool,
            new Not(expr)
        );
    }

    /**
     * Multiplication-like operation
     * @param e Operation
     * @param env Environment
     * @return Annotated expression
     * @see MulOpVisitor
     */
    @Override
    public AnnotatedExpr<EMul> visit(EMul e, EnvTypecheck env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

        if (left.getType() != right.getType()) {
            throw new InvalidOperationException(
                e.mulop_.accept(new MulOpVisitor(), null),
                left.getType(),
                right.getType()
            );
        }

        if (e.mulop_ instanceof Mod) {
            if (left.getType() != TypeCode.CInt) {
                throw new InvalidOperationException(
                    e.mulop_.accept(new MulOpVisitor(), null),
                    left.getType(),
                    TypeCode.CInt
                );
            }
        }

        if (left.getType() != TypeCode.CInt &&
            left.getType() != TypeCode.CDouble) {
            throw new InvalidOperationException(
                e.mulop_.accept(new MulOpVisitor(), null),
                left.getType(),
                TypeCode.CInt,
                TypeCode.CDouble
            );
        }

        return new AnnotatedExpr<>(
            left.getType(),
            new EMul(
                left,
                e.mulop_,
                right
            )
        );
    }

    /**
     * Addition-like operation
     * @param e Operation
     * @param env Environment
     * @return Annotated expression
     * @see AddOpVisitor
     */
    @Override
    public AnnotatedExpr<EAdd> visit(EAdd e, EnvTypecheck env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

        if (left.getType() != right.getType()) {
            throw new InvalidOperationException(
                e.addop_.accept(new AddOpVisitor(), null),
                left.getType(),
                right.getType()
            );
        }

        if (left.getType() != TypeCode.CInt &&
            left.getType() != TypeCode.CDouble) {
            throw new InvalidOperationException(
                e.addop_.accept(new AddOpVisitor(), null),
                left.getType(),
                TypeCode.CInt,
                TypeCode.CDouble
            );
        }

        return new AnnotatedExpr<>(
            left.getType(),
            new EAdd(
                left,
                e.addop_,
                right
            )
        );
    }

    /**
     * Relational operation
     * @param e Operation
     * @param env Environment
     * @return Annotated expression
     * @see RelOpVisitor
     */
    @Override
    public AnnotatedExpr<ERel> visit(ERel e, EnvTypecheck env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
        String opName = e.relop_.accept(
            new RelOpVisitor(left.getType(), right.getType()),
            null
        );

        if (opName != null) {
            throw new InvalidOperationException(
                opName,
                left.getType(),
                right.getType()
            );
        }

        return new AnnotatedExpr<>(
            TypeCode.CBool,
            new ERel(
                left,
                e.relop_,
                right
            )
        );
    }

    /**
     * Disjunction
     * @param e Disjunction
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<EAnd> visit(EAnd e, EnvTypecheck env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

        if (left.getType() != TypeCode.CBool ||
            right.getType() != TypeCode.CBool) {
            throw new InvalidOperationException(
                "conjunction",
                left.getType(),
                right.getType()
            );
        }

        return new AnnotatedExpr<>(
            TypeCode.CBool,
            new EAnd(
                left,
                right
            )
        );
    }

    /**
     * Conjunction
     * @param e Conjunction
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<EOr> visit(EOr e, EnvTypecheck env) {
        AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
        AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

        if (left.getType() != TypeCode.CBool ||
            right.getType() != TypeCode.CBool) {
            throw new InvalidOperationException(
                "disjunction",
                left.getType(),
                right.getType()
            );
        }

        return new AnnotatedExpr<>(
            TypeCode.CBool,
            new EOr(
                left,
                right
            )
        );
    }
}
