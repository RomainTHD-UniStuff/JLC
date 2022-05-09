package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.internal.NotImplementedException;
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
        if ((var.getType().isPrimitive() && var.getPointerLevel() > 0)
            || (var.getType().isObject()) && var.getPointerLevel() > 1) {
            Variable tmp = env.createTempVar(var.getType(), String.format(
                "var_%s",
                var.getSourceName()
            ), var.getPointerLevel() - 1);
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
     * Function call. Note that `getName` of a function should not be used in
     * this visitor because it contains the logical name of the function, not
     * the name of the function in the LLVM IR
     * @param p Function call
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EApp p, EnvCompiler env) {
        FunType func;
        String fName;
        List<OperationItem> args = new ArrayList<>();
        ListExpr listExpr = p.listexpr_;
        List<FunArg> funArgs = new ArrayList<>();

        if (p.expr_ instanceof EVar) {
            // Normal function call

            fName = ((EVar) p.expr_).ident_;
            func = env.lookupFun(fName);
            assert func != null;
        } else if (p.expr_ instanceof EDot) {
            // Class method

            EDot dot = ((EDot) p.expr_);
            assert dot.expr_ instanceof EVar;
            EVar left = (EVar) dot.expr_;

            Variable ref = env.lookupVar(left.ident_);
            assert ref != null;

            ClassType c = env.lookupClass(ref.getType());
            assert c != null;

            while ((func = c.getMethod(dot.ident_, false)) == null) {
                c = c.getSuperclass();
                assert c != null;
            }

            // call `@Class$method` instead of `@method`
            fName = c.getAssemblyMethodName(dot.ident_);

            // Add `this` to the arguments by adding the variable itself. Either
            //  it is a "real" variable like `obj.call()`, or a temporary one
            //  if a cast or deref is involved
            listExpr.add(0, new EVar(ref.getSourceName()));

            if (listExpr.size() != func.getArgs().size()) {
                // FIXME: Makes no sense at all, it means that some methods
                //  don't have the `this` argument in first position!?
                funArgs.add(new FunArg(ref.getType(), "self"));
            }
        } else {
            throw new IllegalStateException("Unsupported function call");
        }

        funArgs.addAll(func.getArgs());

        for (int i = 0; i < listExpr.size(); i++) {
            // Visit arguments
            Expr expr = listExpr.get(i);
            FunArg arg = funArgs.get(i);
            OperationItem value = expr.accept(new ExprVisitor(), env);

            if (!func.getName().equals(ClassType.CONSTRUCTOR_NAME)) {
                // FIXME: Truly awful hack, the constructor's `self` sometimes
                //  doesn't have the right type. Is is ok-ish to just ignore it
                //  for now, because constructors don't need casting

                if (value.getType() != arg.getType()) {
                    value = LLVMCompiler.castTo(
                        arg.getType(),
                        value,
                        env
                    );
                }
            }

            args.add(value);
        }

        if (func.getRetType() == CVoid) {
            env.emit(env.instructionBuilder.call(fName, args));
            return null;
        } else {
            // Return value
            Variable out = env.createTempVar(
                func.getRetType(),
                "function_call",
                func.getRetType().isPrimitive() ? 0 : 1
            );
            env.emit(env.instructionBuilder.call(out, fName, args));
            return out;
        }
    }

    /**
     * Dot operator. Method calls are already checked in visit(EApp), so the
     * only attribute allowed should be `length` for arrays
     * @param p Dot operator
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EDot p, EnvCompiler env) {
        OperationItem left = p.expr_.accept(new ExprVisitor(), env);

        //
        assert p.ident_.equals("length");
        assert left.getType().isArray();

        Variable res = env.createTempVar(CInt, "array_length");
        env.emit(env.instructionBuilder.loadAttribute(res, left, 0));
        return res;
    }

    /**
     * Array access
     * @param p Array access
     * @param env Environment
     * @return Operation result
     */
    @Override
    public OperationItem visit(EIndex p, EnvCompiler env) {
        throw new NotImplementedException();
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
     * Object creation using `new`
     * @param p Object creation
     * @param env Environment
     * @return Operation result
     * @see ExprVisitor#visit(EApp, EnvCompiler)
     */
    @Override
    public OperationItem visit(ENew p, EnvCompiler env) {
        TypeCode type = TypeCode.forArray(
            p.basetype_.accept(new TypeVisitor(), null),
            p.listindex_.size()
        );

        Variable ref = env.createTempVar(
            type,
            "new_" + type.getReadableAssemblyName(),
            1
        );
        env.insertVar(ref.getName(), ref);

        Variable tmp = env.createTempVar(
            TypeCode.CRawPointer,
            "malloc_" + type.getReadableAssemblyName()
        );

        int size = type.getSize();
        if (type.isObject()) {
            // For objects, the size needs to be looked up in the class
            ClassType c = env.lookupClass(type);
            assert c != null;
            size = c.getSize();
        }

        env.emit(env.instructionBuilder.newObject(ref, tmp, size));

        if (type.isObject()) {
            // Call the constructor, which is a method of the object
            new EApp(
                new EDot(
                    new EVar(ref.getName()),
                    ClassType.CONSTRUCTOR_NAME
                ),
                new ListExpr()
            ).accept(new ExprVisitor(), env);
        } else if (type.isArray()) {
            // TODO: Multi-dimensional arrays
            Variable lenField = env.createTempVar(CInt, "array_length", 1);

            OperationItem len = p.listindex_.get(0).accept(
                new IndexVisitor(),
                env
            );

            env.emit(env.instructionBuilder.loadAttribute(lenField, ref, 0));
            env.emit(env.instructionBuilder.store(lenField, len));

            TypeCode contentType = TypeCode.forArray(
                type.getBaseType(),
                type.getDimension() - 1
            );
            Variable contentField = env.createTempVar(
                contentType,
                "array_content",
                2
            );
            Variable contentPtr = env.createTempVar(
                contentType,
                "array_content_ptr",
                1
            );
            Variable contentTmp = env.createTempVar(
                TypeCode.CRawPointer,
                "array_content_ptr"
                // Hack, but no pointer because CRawPointer is already a pointer
            );
            env.emit(env.instructionBuilder.loadAttribute(
                contentField,
                ref,
                1
            ));
            env.emit(env.instructionBuilder.arrayAlloc(
                contentPtr,
                contentTmp,
                len,
                contentType
            ));
            env.emit(env.instructionBuilder.store(contentField, contentPtr));
        }

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
