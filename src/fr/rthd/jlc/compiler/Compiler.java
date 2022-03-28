package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.AddOp;
import javalette.Absyn.Ass;
import javalette.Absyn.BStmt;
import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.Div;
import javalette.Absyn.EAdd;
import javalette.Absyn.EAnd;
import javalette.Absyn.EApp;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EMul;
import javalette.Absyn.EOr;
import javalette.Absyn.EQU;
import javalette.Absyn.ERel;
import javalette.Absyn.EString;
import javalette.Absyn.EVar;
import javalette.Absyn.Empty;
import javalette.Absyn.Expr;
import javalette.Absyn.FnDef;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.Incr;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.Minus;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.NE;
import javalette.Absyn.Neg;
import javalette.Absyn.NoInit;
import javalette.Absyn.Not;
import javalette.Absyn.Plus;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.RelOp;
import javalette.Absyn.Ret;
import javalette.Absyn.SExp;
import javalette.Absyn.Stmt;
import javalette.Absyn.Times;
import javalette.Absyn.TopDef;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    private static InstructionBuilder instructionBuilder;

    public Compiler(InstructionBuilder builder) {
        instructionBuilder = builder;
    }

    public String compile(Prog p, Env<?, FunType> parent) {
        EnvCompiler env = new EnvCompiler(parent);
        p.accept(new ProgVisitor(), env);
        return env.toAssembly();
    }

    public static class ProgVisitor implements Prog.Visitor<Void, EnvCompiler> {
        public Void visit(Program p, EnvCompiler env) {
            for (TopDef topdef : p.listtopdef_) {
                topdef.accept(new TopDefVisitor(), env);
            }
            return null;
        }
    }

    public static class TopDefVisitor implements TopDef.Visitor<Void, EnvCompiler> {
        public Void visit(FnDef p, EnvCompiler env) {
            FunType func = env.lookupFun(p.ident_);

            env.resetScope();

            func.args.forEach(arg -> {
                Variable var = env.createVar(arg.type, arg.name);
                env.insertVar(arg.name, var);
                arg.setGeneratedName(var.name);
            });

            env.emit(instructionBuilder.functionDeclarationStart(func));
            env.emit(instructionBuilder.label("entry"));

            p.blk_.accept(new BlkVisitor(), env);

            env.emit(instructionBuilder.functionDeclarationEnd());

            return null;
        }
    }

    public static class ExprVisitor implements Expr.Visitor<OperationItem, EnvCompiler> {
        public OperationItem visit(EVar p, EnvCompiler env) {
            return env.lookupVar(p.ident_);
        }

        public OperationItem visit(ELitInt p, EnvCompiler env) {
            return new Literal(TypeCode.CInt, p.integer_);
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

        public OperationItem visit(EApp p, EnvCompiler env) {
            List<OperationItem> args = new ArrayList<>();

            for (Expr expr : p.listexpr_) {
                args.add(expr.accept(new ExprVisitor(), env));
            }

            FunType func = env.lookupFun(p.ident_);

            if (func.retType == TypeCode.CVoid) {
                env.emit(instructionBuilder.call(func.name, args));
                return null;
            } else {
                Variable out = env.createTempVar(
                    func.retType,
                    "function_call"
                );
                env.emit(instructionBuilder.call(out, func.name, args));
                return out;
            }
        }

        public OperationItem visit(EString p, EnvCompiler env) {
            String content = p.string_ + "\n";
            Variable global = env.createGlobalStringLiteral(content);

            if (env.lookupVar(global.name) == null) {
                // Avoid loading the same string literal multiple times
                env.insertVar(global.name, global);
                env.emitAtBeginning(instructionBuilder.globalStringLiteral(
                    global,
                    content
                ));
            }

            Variable tmp = env.createTempVar(TypeCode.CString, "string_literal");
            env.emit(instructionBuilder.loadStringLiteral(tmp, global));
            return tmp;
        }

        public OperationItem visit(Neg p, EnvCompiler env) {
            OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
            Variable var = env.createTempVar(expr.type, "neg");
            env.emit(instructionBuilder.neg(var, expr));
            return var;
        }

        public OperationItem visit(Not p, EnvCompiler env) {
            OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
            Variable var = env.createTempVar(expr.type, "not");
            env.emit(instructionBuilder.not(var, expr));
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
            Variable res = env.createTempVar(TypeCode.CBool, "and");
            env.emit(instructionBuilder.declare(res));

            String trueLabel = env.getNewLabel("andTrue");
            String falseLabel = env.getNewLabel("andFalse");
            String endLabel = env.getNewLabel("andEnd");

            env.emit(instructionBuilder.comment("and"));
            env.indent();
            env.emit(instructionBuilder.comment("and left"));

            OperationItem left = p.expr_1.accept(new ExprVisitor(), env);
            env.emit(instructionBuilder.conditionalJump(
                left,
                trueLabel,
                falseLabel
            ));

            env.emit(instructionBuilder.label(trueLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("and true"));
            env.emit(instructionBuilder.store(
                res,
                p.expr_2.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.emit(instructionBuilder.label(falseLabel));
            env.emit(instructionBuilder.comment("and false"));
            env.emit(instructionBuilder.store(
                res,
                new Literal(TypeCode.CBool, false)
            ));
            env.emit(instructionBuilder.jump(endLabel));

            env.unindent();
            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endand"));
            env.emit(instructionBuilder.newLine());

            return res;
        }

        public OperationItem visit(EOr p, EnvCompiler env) {
            Variable res = env.createTempVar(TypeCode.CBool, "or");
            env.emit(instructionBuilder.declare(res));

            String trueLabel = env.getNewLabel("orTrue");
            String falseLabel = env.getNewLabel("orFalse");
            String endLabel = env.getNewLabel("orEnd");

            env.emit(instructionBuilder.comment("or"));
            env.indent();
            env.emit(instructionBuilder.comment("or left"));

            OperationItem left = p.expr_1.accept(new ExprVisitor(), env);
            env.emit(instructionBuilder.conditionalJump(
                left,
                trueLabel,
                falseLabel
            ));

            env.emit(instructionBuilder.label(trueLabel));
            env.emit(instructionBuilder.comment("or true"));
            env.emit(instructionBuilder.store(
                res,
                new Literal(TypeCode.CBool, true)
            ));
            env.emit(instructionBuilder.jump(endLabel));

            env.emit(instructionBuilder.label(falseLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("or false"));
            env.emit(instructionBuilder.store(
                res,
                p.expr_2.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.unindent();
            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endor"));
            env.emit(instructionBuilder.newLine());

            return res;
        }
    }

    public static class BlkVisitor implements Blk.Visitor<Void, EnvCompiler> {
        public Void visit(Block p, EnvCompiler env) {
            env.emit(instructionBuilder.comment("start block"));
            env.emit(instructionBuilder.newLine());
            env.indent();

            env.enterScope();
            for (Stmt s : p.liststmt_) {
                s.accept(new StmtVisitor(), env);
            }
            env.leaveScope();

            env.unindent();
            env.emit(instructionBuilder.comment("end block"));
            env.emit(instructionBuilder.newLine());
            return null;
        }
    }

    public static class StmtVisitor implements Stmt.Visitor<Void, EnvCompiler> {
        public Void visit(Empty p, EnvCompiler env) {
            env.emit(instructionBuilder.noop());
            return null;
        }

        public Void visit(BStmt p, EnvCompiler env) {
            p.blk_.accept(new BlkVisitor(), env);
            return null;
        }

        public Void visit(Decl p, EnvCompiler env) {
            p.listitem_.forEach(item -> item.accept(
                new ItemVisitor(p.type_.accept(new TypeVisitor(), null)),
                env
            ));
            return null;
        }

        public Void visit(Ass p, EnvCompiler env) {
            env.emit(instructionBuilder.store(
                env.lookupVar(p.ident_),
                p.expr_.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(Incr p, EnvCompiler env) {
            Variable src = env.lookupVar(p.ident_);
            Variable dst = env.createVar(src.type, p.ident_);
            env.emit(instructionBuilder.increment(
                dst,
                src
            ));
            env.emit(instructionBuilder.newLine());
            env.updateVar(p.ident_, dst);
            return null;
        }

        public Void visit(Decr p, EnvCompiler env) {
            Variable src = env.lookupVar(p.ident_);
            Variable dst = env.createVar(src.type, p.ident_);
            env.emit(instructionBuilder.decrement(
                dst,
                src
            ));
            env.emit(instructionBuilder.newLine());
            env.updateVar(p.ident_, dst);
            return null;
        }

        public Void visit(Ret p, EnvCompiler env) {
            env.emit(instructionBuilder.ret(
                p.expr_.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(VRet p, EnvCompiler env) {
            env.emit(instructionBuilder.ret());
            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(Cond p, EnvCompiler env) {
            String thenLabel = env.getNewLabel("ifTrue");
            String endLabel = env.getNewLabel("ifEnd");

            env.emit(instructionBuilder.comment("if"));
            env.indent();
            env.emit(instructionBuilder.comment("if exp"));

            OperationItem res = p.expr_.accept(new ExprVisitor(), env);
            env.emit(instructionBuilder.conditionalJump(
                res,
                thenLabel,
                endLabel
            ));

            env.emit(instructionBuilder.label(thenLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("if then"));
            p.stmt_.accept(new StmtVisitor(), env);
            // Not useful to emit a jump here since there is a fallthrough
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.unindent();
            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endif"));

            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(CondElse p, EnvCompiler env) {
            String thenLabel = env.getNewLabel("ifTrue");
            String elseLabel = env.getNewLabel("ifFalse");
            String endLabel = env.getNewLabel("ifEnd");

            env.emit(instructionBuilder.comment("if"));
            env.indent();
            env.emit(instructionBuilder.comment("if exp"));

            OperationItem res = p.expr_.accept(new ExprVisitor(), env);
            env.emit(instructionBuilder.conditionalJump(
                res,
                thenLabel,
                elseLabel
            ));

            env.emit(instructionBuilder.label(thenLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("if then"));
            p.stmt_1.accept(new StmtVisitor(), env);
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.emit(instructionBuilder.label(elseLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("if else"));
            p.stmt_2.accept(new StmtVisitor(), env);
            // Not useful to emit a jump here since there is a fallthrough
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.unindent();
            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endif"));

            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(While p, EnvCompiler env) {
            String cmpLabel = env.getNewLabel("whileCompare");
            String loopLabel = env.getNewLabel("whileLoop");
            String endLabel = env.getNewLabel("whileEnd");

            env.emit(instructionBuilder.comment("while"));
            env.emit(instructionBuilder.jump(cmpLabel));
            env.indent();
            env.emit(instructionBuilder.label(cmpLabel));
            env.emit(instructionBuilder.comment("while exp"));

            OperationItem res = p.expr_.accept(new ExprVisitor(), env);
            env.emit(instructionBuilder.conditionalJump(
                res,
                loopLabel,
                endLabel
            ));

            env.emit(instructionBuilder.label(loopLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("while loop"));
            p.stmt_.accept(new StmtVisitor(), env);
            env.emit(instructionBuilder.jump(cmpLabel));
            env.leaveScope();
            env.unindent();

            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endwhile"));

            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(SExp p, EnvCompiler env) {
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }
    }

    public static class ItemVisitor implements Item.Visitor<Void, EnvCompiler> {
        private final TypeCode type;

        public ItemVisitor(TypeCode type) {
            this.type = type;
        }

        public Void visit(NoInit p, EnvCompiler env) {
            env.insertVar(p.ident_, env.createVar(type, p.ident_));
            env.emit(instructionBuilder.declare(
                env.lookupVar(p.ident_)
            ));
            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(Init p, EnvCompiler env) {
            env.insertVar(p.ident_, env.createVar(type, p.ident_));
            env.emit(instructionBuilder.declare(
                env.lookupVar(p.ident_)
            ));
            env.emit(instructionBuilder.store(
                env.lookupVar(p.ident_),
                p.expr_.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.newLine());
            return null;
        }
    }

    public static class AddOpVisitor implements AddOp.Visitor<OperationItem, EnvCompiler> {
        private final OperationItem left;
        private final OperationItem right;

        public AddOpVisitor(OperationItem left, OperationItem right) {
            this.left = left;
            this.right = right;
        }

        public OperationItem visit(Plus p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "add");
            env.emit(instructionBuilder.add(var, left, right));
            return var;
        }

        public OperationItem visit(Minus p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "sub");
            env.emit(instructionBuilder.subtract(var, left, right));
            return var;
        }
    }

    public static class MulOpVisitor implements MulOp.Visitor<OperationItem, EnvCompiler> {
        private final OperationItem left;
        private final OperationItem right;

        public MulOpVisitor(OperationItem left, OperationItem right) {
            this.left = left;
            this.right = right;
        }

        public OperationItem visit(Times p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "mult");
            env.emit(instructionBuilder.multiply(var, left, right));
            return var;
        }

        public OperationItem visit(Div p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "div");
            env.emit(instructionBuilder.divide(var, left, right));
            return var;
        }

        public OperationItem visit(Mod p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "mod");
            env.emit(instructionBuilder.modulo(var, left, right));
            return var;
        }
    }

    public static class RelOpVisitor implements RelOp.Visitor<OperationItem, EnvCompiler> {
        private final OperationItem left;
        private final OperationItem right;

        public RelOpVisitor(OperationItem left, OperationItem right) {
            this.left = left;
            this.right = right;
        }

        public OperationItem visit(LTH p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "lt");
            env.emit(instructionBuilder.compare(var, left, ComparisonOperator.LT, right));
            return var;
        }

        public OperationItem visit(LE p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "le");
            env.emit(instructionBuilder.compare(var, left, ComparisonOperator.LE, right));
            return var;
        }

        public OperationItem visit(GTH p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "gt");
            env.emit(instructionBuilder.compare(var, left, ComparisonOperator.GT, right));
            return var;
        }

        public OperationItem visit(GE p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "ge");
            env.emit(instructionBuilder.compare(var, left, ComparisonOperator.GE, right));
            return var;
        }

        public OperationItem visit(EQU p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "eq");
            env.emit(instructionBuilder.compare(var, left, ComparisonOperator.EQ, right));
            return var;
        }

        public OperationItem visit(NE p, EnvCompiler env) {
            Variable var = env.createTempVar(left.type, "ne");
            env.emit(instructionBuilder.compare(var, left, ComparisonOperator.NE, right));
            return var;
        }
    }
}
