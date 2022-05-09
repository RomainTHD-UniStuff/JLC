package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typechecker.exception.InvalidArgumentCountException;
import fr.rthd.jlc.typechecker.exception.InvalidAssignmentTypeException;
import fr.rthd.jlc.typechecker.exception.InvalidFieldAccessException;
import fr.rthd.jlc.typechecker.exception.InvalidFunctionCallException;
import fr.rthd.jlc.typechecker.exception.InvalidIndexDimensionException;
import fr.rthd.jlc.typechecker.exception.InvalidNewTypeException;
import fr.rthd.jlc.typechecker.exception.InvalidOperationException;
import fr.rthd.jlc.typechecker.exception.NoSuchClassException;
import fr.rthd.jlc.typechecker.exception.NoSuchFunctionException;
import fr.rthd.jlc.typechecker.exception.NoSuchVariableException;
import fr.rthd.jlc.typechecker.exception.SelfOutOfClassException;
import javalette.Absyn.EAdd;
import javalette.Absyn.EAnd;
import javalette.Absyn.EApp;
import javalette.Absyn.EDot;
import javalette.Absyn.EIndex;
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
import javalette.Absyn.Index;
import javalette.Absyn.ListExpr;
import javalette.Absyn.ListIndex;
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
        if (e.ident_.equals("self")) {
            ClassType c = env.getCurrentClass();
            if (c == null) {
                throw new SelfOutOfClassException();
            }
        }

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
     * Function call
     * @param e Function call
     * @param env Environment
     * @return Annotated expression
     */
    @Override
    public AnnotatedExpr<EApp> visit(EApp e, EnvTypecheck env) {
        FunType funcType;
        String methodName;

        if (e.expr_ instanceof EVar) {
            // `f()`
            methodName = ((EVar) e.expr_).ident_;
            funcType = env.lookupFun(methodName);
        } else if (e.expr_ instanceof EDot) {
            // `self.f()`
            AnnotatedExpr<?> left = e.expr_.accept(new ExprVisitor(), env);
            ClassType c = env.lookupClass(left.getType());
            assert c != null;
            methodName = ((EDot) e.expr_).ident_;
            funcType = c.getMethod(methodName, true);
        } else {
            // `42()`
            throw new InvalidFunctionCallException();
        }

        if (funcType == null) {
            throw new NoSuchFunctionException(methodName);
        }

        if (e.listexpr_.size() != funcType.getArgs().size()) {
            throw new InvalidArgumentCountException(
                methodName,
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
                    exp.getType()
                );
            }
            exps.add(exp);
        }

        return new AnnotatedExpr<>(
            funcType.getRetType(),
            new EApp(e.expr_, exps)
        );
    }

    /**
     * Method call
     * @param e Method call
     * @param env Environment
     * @return Annotated expression
     * @see ExprVisitor#visit(EApp, EnvTypecheck)
     */
    @Override
    public AnnotatedExpr<EDot> visit(EDot e, EnvTypecheck env) {
        AnnotatedExpr<?> expr = e.expr_.accept(
            new ExprVisitor(),
            env
        );

        if (expr.getType().isArray()) {
            if (e.ident_.equals("length")) {
                return new AnnotatedExpr<>(
                    TypeCode.CInt,
                    new EDot(expr, e.ident_)
                );
            } else {
                throw new InvalidFieldAccessException(e.ident_, expr.getType());
            }
        } else if (expr.getType().isObject()) {
            return new AnnotatedExpr<>(
                // HACK: Technically, this type is wrong, but it makes it easier
                //  if we're in a method call
                expr.getType(),
                new EDot(
                    expr,
                    e.ident_
                )
            );
        } else {
            throw new InvalidFieldAccessException(e.ident_, expr.getType());
        }
    }

    @Override
    public AnnotatedExpr<EIndex> visit(EIndex e, EnvTypecheck env) {
        Index idx = e.index_.accept(new IndexVisitor(0), env);
        ListIndex indexes = new ListIndex();
        for (int i = 0; i < e.listindex_.size(); ++i) {
            indexes.add(e.listindex_.get(i).accept(
                new IndexVisitor(i + 1),
                env
            ));
        }

        AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);

        int newDim = expr.getType().getDimension() - indexes.size() - 1;
        if (newDim < 0) {
            throw new InvalidIndexDimensionException(
                expr.getType().getDimension(),
                indexes.size() + 1
            );
        }

        return new AnnotatedExpr<>(
            TypeCode.forArray(expr.getType().getBaseType(), newDim),
            new EIndex(expr, idx, indexes)
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
        TypeCode t = e.basetype_.accept(new TypeVisitor(), null);

        if (!e.listindex_.isEmpty()) {
            t = TypeCode.forArray(t, e.listindex_.size());
        }

        for (int i = 0; i < e.listindex_.size(); ++i) {
            // Populate all intermediate types, used for compiler
            TypeCode.forArray(t, i);
        }

        ListIndex listIndex = new ListIndex();
        for (int i = 0; i < e.listindex_.size(); ++i) {
            listIndex.add(e.listindex_.get(i).accept(
                new IndexVisitor(i),
                env
            ));
        }

        if (t.isPrimitive()) {
            throw new InvalidNewTypeException(t);
        } else if (t.isObject()) {
            if (env.lookupClass(t) == null) {
                throw new NoSuchClassException(t);
            }
        }

        return new AnnotatedExpr<>(t, new ENew(e.basetype_, listIndex));
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
