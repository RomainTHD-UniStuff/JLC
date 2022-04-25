package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
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
import javalette.Absyn.Neg;
import javalette.Absyn.Not;

import java.util.ArrayList;
import java.util.List;

import static fr.rthd.jlc.TypeCode.CInt;

class ExprVisitor implements Expr.Visitor<OperationItem, EnvCompiler> {
    public OperationItem visit(ENull p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    public OperationItem visit(EVar p, EnvCompiler env) {
        Variable var = env.lookupVar(p.ident_);
        if (var.isPointer()) {
            Variable tmp = env.createTempVar(var.type, String.format(
                "var_%s",
                var.name.replace(EnvCompiler.SEP, '-')
            ));
            env.emit(env.instructionBuilder.load(tmp, var));
            return tmp;
        } else {
            return var;
        }
    }

    public OperationItem visit(ELitInt p, EnvCompiler env) {
        return new Literal(CInt, p.integer_);
    }

    public OperationItem visit(ELitDoub p, EnvCompiler env) {
        return new Literal(TypeCode.CDouble, p.double_);
    }

    public OperationItem visit(ELitTrue p, EnvCompiler env) {
        return new Literal(TypeCode.CBool, true);
    }

    public OperationItem visit(ELitFalse p, EnvCompiler env) {
        return new Literal(TypeCode.CBool, false);
    }

    public OperationItem visit(ESelf p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    public OperationItem visit(EApp p, EnvCompiler env) {
        List<OperationItem> args = new ArrayList<>();

        for (Expr expr : p.listexpr_) {
            args.add(expr.accept(new ExprVisitor(), env));
        }

        FunType func = env.lookupFun(p.ident_);

        if (func.retType == TypeCode.CVoid) {
            env.emit(env.instructionBuilder.call(func.name, args));
            return null;
        } else {
            Variable out = env.createTempVar(
                func.retType,
                "function_call"
            );
            env.emit(env.instructionBuilder.call(out, func.name, args));
            return out;
        }
    }

    public OperationItem visit(EString p, EnvCompiler env) {
        String content = p.string_;
        Variable global = env.createGlobalStringLiteral(content);

        if (env.lookupVar(global.name) == null) {
            // Avoid loading the same string literal multiple times
            env.insertVar(global.name, global);
            env.emitAtBeginning(env.instructionBuilder.globalStringLiteral(
                global,
                content
            ));
        }

        Variable tmp = env.createTempVar(
            TypeCode.CString,
            "string_literal"
        );
        env.emit(env.instructionBuilder.loadStringLiteral(tmp, global));
        return tmp;
    }

    public OperationItem visit(EDot p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    public OperationItem visit(ENew p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    public OperationItem visit(Neg p, EnvCompiler env) {
        OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
        if (expr instanceof Literal) {
            Literal lit = (Literal) expr;
            if (lit.type == CInt) {
                return new Literal(CInt, -(int) lit.value);
            } else if (lit.type == TypeCode.CDouble) {
                return new Literal(TypeCode.CDouble, -(double) lit.value);
            } else {
                throw new RuntimeException("Unsupported type for negation");
            }
        } else {
            Variable var = env.createTempVar(expr.type, "neg");
            env.emit(env.instructionBuilder.neg(var, (Variable) expr));
            return var;
        }
    }

    public OperationItem visit(Not p, EnvCompiler env) {
        OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
        Variable var = env.createTempVar(expr.type, "not");
        env.emit(env.instructionBuilder.not(var, expr));
        return var;
    }

    public OperationItem visit(EMul p, EnvCompiler env) {
        return p.mulop_.accept(new MulOpVisitor(
            p.expr_1.accept(new ExprVisitor(), env),
            p.expr_2.accept(new ExprVisitor(), env)
        ), env);
    }

    public OperationItem visit(EAdd p, EnvCompiler env) {
        return p.addop_.accept(new AddOpVisitor(
            p.expr_1.accept(new ExprVisitor(), env),
            p.expr_2.accept(new ExprVisitor(), env)
        ), env);
    }

    public OperationItem visit(ERel p, EnvCompiler env) {
        return p.relop_.accept(new RelOpVisitor(
            p.expr_1.accept(new ExprVisitor(), env),
            p.expr_2.accept(new ExprVisitor(), env)
        ), env);
    }

    public OperationItem visit(EAnd p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "and_ptr", true);
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
            new Literal(TypeCode.CBool, false)
        ));
        env.emit(env.instructionBuilder.jump(endLabel));

        env.unindent();
        env.emit(env.instructionBuilder.label(endLabel));
        env.emit(env.instructionBuilder.comment("endand"));
        env.emit(env.instructionBuilder.newLine());

        Variable tmp = env.createTempVar(var.type, "and");
        env.emit(env.instructionBuilder.load(tmp, var));
        return tmp;
    }

    public OperationItem visit(EOr p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "or_ptr", true);
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
            new Literal(TypeCode.CBool, true)
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
        env.emit(env.instructionBuilder.comment("endor"));
        env.emit(env.instructionBuilder.newLine());

        Variable tmp = env.createTempVar(var.type, "or");
        env.emit(env.instructionBuilder.load(tmp, var));
        return tmp;
    }
}
