package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.internal.NotImplementedException;
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
import javalette.Absyn.Neg;
import javalette.Absyn.Not;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

import static fr.rthd.jlc.TypeCode.CBool;
import static fr.rthd.jlc.TypeCode.CDouble;
import static fr.rthd.jlc.TypeCode.CInt;
import static fr.rthd.jlc.TypeCode.CString;
import static fr.rthd.jlc.TypeCode.CVoid;

/**
 * Expression visitor
 * @author RomainTHD
 */
@NonNls
class ExprVisitor implements Expr.Visitor<OperationItem, EnvCompiler> {
    /**
     * Null literal
     * @param p Null literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(ENull p, EnvCompiler env) {
        return new Literal(TypeCode.forClass(p.ident_), null, 1);
    }

    /**
     * Variable literal
     * @param p Variable literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EVar p, EnvCompiler env) {
        Variable var = env.lookupVar(p.ident_);
        assert var != null;
        if ((var.getType().isPrimitive() && var.getPointerLevel() > 0)) {
            // If the variable is a pointer to a primitive type, we need to
            //  dereference it using a temp variable to respect our
            //  convention that all returned values are non-pointer values. We
            //  also want to dereference if we are in an assignment with
            //  objects, to respect code like `Object a = b`
            Variable tmp = env.createTempVar(var.getType(), String.format(
                "var_%s",
                var.getName().replace(EnvCompiler.SEP, '-')
            ));
            env.emit(env.instructionBuilder.load(tmp, var));
            return tmp;
        } else {
            return var;
        }
    }

    /**
     * Integer literal
     * @param p Integer literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(ELitInt p, EnvCompiler env) {
        return new Literal(CInt, p.integer_);
    }

    /**
     * Double literal
     * @param p Double literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(ELitDoub p, EnvCompiler env) {
        return new Literal(CDouble, p.double_);
    }

    /**
     * Boolean true literal
     * @param p Boolean true literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(ELitTrue p, EnvCompiler env) {
        return new Literal(CBool, true);
    }

    /**
     * Boolean false literal
     * @param p Boolean false literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(ELitFalse p, EnvCompiler env) {
        return new Literal(CBool, false);
    }

    /**
     * Self expression
     * @param p Self expression
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(ESelf p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    /**
     * Function call
     * @param p Function call
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EApp p, EnvCompiler env) {
        List<OperationItem> args = new ArrayList<>();

        for (Expr expr : p.listexpr_) {
            // Visit arguments
            args.add(expr.accept(new ExprVisitor(), env));
        }

        FunType func = env.lookupFun(p.ident_);
        assert func != null;

        // `getName` should not be used in this visitor because it contains the
        //  logical name of the function, not the name of the function in the
        //  LLVM IR

        if (func.getRetType() == CVoid) {
            env.emit(env.instructionBuilder.call(p.ident_, args));
            return null;
        } else {
            // Return value
            Variable out = env.createTempVar(
                func.getRetType(),
                "function_call"
            );
            env.emit(env.instructionBuilder.call(out, p.ident_, args));
            return out;
        }
    }

    /**
     * String literal
     * @param p String literal
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EString p, EnvCompiler env) {
        String content = p.string_;
        Variable global = env.createGlobalStringLiteral(content);

        if (env.lookupVar(global.getName()) == null) {
            // Avoid loading the same string literal multiple times by emitting
            //  a global variable only if it is not already defined
            env.insertVar(global.getName(), global);
            env.emitAtBeginning(env.instructionBuilder.globalStringLiteral(
                global,
                content
            ));
        }

        // Load the global variable into a local variable
        Variable tmp = env.createTempVar(
            CString,
            "string_literal"
        );
        env.emit(env.instructionBuilder.loadStringLiteral(tmp, global));
        return tmp;
    }

    /**
     * Method call
     * @param p Method call
     * @param env Environment
     * @return Operation result
     * @see ExprVisitor#visit(EApp, EnvCompiler)
     */
    @Override
    public OperationItem visit(EDot p, EnvCompiler env) {
        TypeCode classType = ((AnnotatedExpr<?>) p.expr_).getType();
        ClassType c = env.lookupClass(classType);
        assert c != null;

        // Add `this` to the arguments
        ListExpr args = new ListExpr();
        args.add(p.expr_);
        args.addAll(p.listexpr_);

        // Call the "normal" function call visitor
        return new EApp(
            c.getAssemblyMethodName(p.ident_),
            args
        ).accept(new ExprVisitor(), env);
    }

    /**
     * Object creation using `new`
     * @param p Object creation
     * @param env Environment
     * @return Operation result
     * @see ExprVisitor#visit(EDot, EnvCompiler)
     */
    @Override
    public OperationItem visit(ENew p, EnvCompiler env) {
        TypeCode classType = p.type_.accept(new TypeVisitor(), null);
        Variable ref = env.createTempVar(classType, "new", 2);

        ClassType c = env.lookupClass(classType);
        assert c != null;

        env.insertVar(ref.getName(), ref);

        // Call the constructor, which is a method of the object
        new EDot(
            new AnnotatedExpr<>(classType, new EVar(ref.getName())),
            c.getConstructorName(),
            new ListExpr()
        ).accept(new ExprVisitor(), env);

        return ref;
    }

    /**
     * Mathematical negation
     * @param p Negation
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(Neg p, EnvCompiler env) {
        OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
        if (expr instanceof Literal) {
            // `-(5)` is the same as `-5`
            // FIXME: Should already be handled by the optimizer?
            Literal lit = (Literal) expr;
            assert lit.getValue() != null;
            if (lit.getType() == CInt) {
                return new Literal(CInt, -(int) lit.getValue());
            } else if (lit.getType() == CDouble) {
                return new Literal(CDouble, -(double) lit.getValue());
            } else {
                throw new IllegalArgumentException(
                    "Unsupported type for negation");
            }
        } else {
            Variable var = env.createTempVar(expr.getType(), "neg");
            env.emit(env.instructionBuilder.neg(var, (Variable) expr));
            return var;
        }
    }

    /**
     * Logical negation
     * @param p Negation
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(Not p, EnvCompiler env) {
        OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
        Variable var = env.createTempVar(expr.getType(), "not");
        env.emit(env.instructionBuilder.not(var, expr));
        return var;
    }

    /**
     * Multiplication-like operation
     * @param p Multiplication
     * @param env Environment
     * @return Operation result
     * @see MulOpVisitor
     */
    @Override
    public OperationItem visit(EMul p, EnvCompiler env) {
        return p.mulop_.accept(new MulOpVisitor(
            p.expr_1.accept(new ExprVisitor(), env),
            p.expr_2.accept(new ExprVisitor(), env)
        ), env);
    }

    /**
     * Addition-like operation
     * @param p Addition
     * @param env Environment
     * @return Operation result
     * @see AddOpVisitor
     */
    @Override
    public OperationItem visit(EAdd p, EnvCompiler env) {
        return p.addop_.accept(new AddOpVisitor(
            p.expr_1.accept(new ExprVisitor(), env),
            p.expr_2.accept(new ExprVisitor(), env)
        ), env);
    }

    /**
     * Logical comparison
     * @param p Comparison
     * @param env Environment
     * @return Operation result
     * @see RelOpVisitor
     */
    @Override
    public OperationItem visit(ERel p, EnvCompiler env) {
        return p.relop_.accept(new RelOpVisitor(
            p.expr_1.accept(new ExprVisitor(), env),
            p.expr_2.accept(new ExprVisitor(), env)
        ), env);
    }

    /**
     * Disjunction
     * @param p Disjunction
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EAnd p, EnvCompiler env) {
        // We need to create a pointer to the result variable
        Variable var = env.createTempVar(CBool, "and_ptr", 1);
        env.emit(env.instructionBuilder.declare(var));

        String trueLabel = env.getNewLabel("and_true");
        String falseLabel = env.getNewLabel("and_false");
        String endLabel = env.getNewLabel("and_end");

        env.emit(env.instructionBuilder.comment("and"));
        env.indent();
        env.emit(env.instructionBuilder.comment("and left"));

        OperationItem left = p.expr_1.accept(new ExprVisitor(), env);
        env.emit(env.instructionBuilder.conditionalJump(
            left,
            trueLabel,
            falseLabel
        ));

        env.emit(env.instructionBuilder.label(trueLabel));
        env.enterScope();
        env.emit(env.instructionBuilder.comment("and true"));
        env.emit(env.instructionBuilder.store(
            var,
            p.expr_2.accept(new ExprVisitor(), env)
        ));
        env.emit(env.instructionBuilder.jump(endLabel));
        env.leaveScope();

        env.emit(env.instructionBuilder.label(falseLabel));
        env.emit(env.instructionBuilder.comment("and false"));
        env.emit(env.instructionBuilder.store(
            var,
            new Literal(CBool, false)
        ));
        env.emit(env.instructionBuilder.jump(endLabel));

        env.unindent();
        env.emit(env.instructionBuilder.label(endLabel));
        env.emit(env.instructionBuilder.comment("end and"));
        env.emit(env.instructionBuilder.newLine());

        Variable tmp = env.createTempVar(var.getType(), "and");
        env.emit(env.instructionBuilder.load(tmp, var));
        return tmp;
    }

    /**
     * Conjunction
     * @param p Conjunction
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EOr p, EnvCompiler env) {
        Variable var = env.createTempVar(CBool, "or_ptr", 1);
        env.emit(env.instructionBuilder.declare(var));

        String trueLabel = env.getNewLabel("or_true");
        String falseLabel = env.getNewLabel("or_false");
        String endLabel = env.getNewLabel("or_end");

        env.emit(env.instructionBuilder.comment("or"));
        env.indent();
        env.emit(env.instructionBuilder.comment("or left"));

        OperationItem left = p.expr_1.accept(new ExprVisitor(), env);
        env.emit(env.instructionBuilder.conditionalJump(
            left,
            trueLabel,
            falseLabel
        ));

        env.emit(env.instructionBuilder.label(trueLabel));
        env.emit(env.instructionBuilder.comment("or true"));
        env.emit(env.instructionBuilder.store(
            var,
            new Literal(CBool, true)
        ));
        env.emit(env.instructionBuilder.jump(endLabel));

        env.emit(env.instructionBuilder.label(falseLabel));
        env.enterScope();
        env.emit(env.instructionBuilder.comment("or false"));
        env.emit(env.instructionBuilder.store(
            var,
            p.expr_2.accept(new ExprVisitor(), env)
        ));
        env.emit(env.instructionBuilder.jump(endLabel));
        env.leaveScope();

        env.unindent();
        env.emit(env.instructionBuilder.label(endLabel));
        env.emit(env.instructionBuilder.comment("end or"));
        env.emit(env.instructionBuilder.newLine());

        Variable tmp = env.createTempVar(var.getType(), "or");
        env.emit(env.instructionBuilder.load(tmp, var));
        return tmp;
    }
}
