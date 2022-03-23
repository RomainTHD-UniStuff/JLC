package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
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
import javalette.Absyn.ListStmt;
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

            env.emit(instructionBuilder.functionDeclarationStart(func));
            env.emit(instructionBuilder.label("entry"));

            env.resetScope();
            env.indent();
            env.enterScope();

            env.leaveScope();
            env.unindent();

            env.emit(instructionBuilder.functionDeclarationEnd());

            return null;
        }
    }

    public static class ExprVisitor implements Expr.Visitor<OperationItem, EnvCompiler> {
        public Variable visit(EVar p, EnvCompiler env) {
            return env.lookupVar(p.ident_);
        }

        public Literal visit(ELitInt p, EnvCompiler env) {
            return new Literal(TypeCode.CInt, p.integer_);
        }

        public Literal visit(ELitDoub p, EnvCompiler env) {
            return new Literal(TypeCode.CDouble, p.double_);
        }

        public Literal visit(ELitTrue p, EnvCompiler env) {
            return new Literal(TypeCode.CBool, true);
        }

        public Literal visit(ELitFalse p, EnvCompiler env) {
            return new Literal(TypeCode.CBool, false);
        }

        public OperationItem visit(EApp p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public OperationItem visit(EString p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public Variable visit(Neg p, EnvCompiler env) {
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
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            ), env);
        }

        public OperationItem visit(EAdd p, EnvCompiler env) {
            return p.addop_.accept(new AddOpVisitor(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            ), env);
        }

        public OperationItem visit(ERel p, EnvCompiler env) {
            return p.relop_.accept(new RelOpVisitor(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            ), env);
        }

        public OperationItem visit(EAnd p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public OperationItem visit(EOr p, EnvCompiler env) {
            // TODO:
            return null;
        }
    }

    public static class BlkVisitor implements Blk.Visitor<Blk, EnvCompiler> {
        public Block visit(Block p, EnvCompiler env) {
            ListStmt stmt = new ListStmt();
            for (Stmt s : p.liststmt_) {
                stmt.add(s.accept(new StmtVisitor(), null));
            }
            return new Block(stmt);
        }
    }

    public static class StmtVisitor implements Stmt.Visitor<Stmt, EnvCompiler> {
        public Empty visit(Empty p, EnvCompiler env) {
            return p;
        }

        public BStmt visit(BStmt p, EnvCompiler env) {
            return new BStmt(p.blk_.accept(new BlkVisitor(), null));
        }

        public Decl visit(Decl p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public Ass visit(Ass p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public Incr visit(Incr p, EnvCompiler env) {
            return p;
        }

        public Decr visit(Decr p, EnvCompiler env) {
            return p;
        }

        public Ret visit(Ret p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public VRet visit(VRet p, EnvCompiler env) {
            return p;
        }

        public Cond visit(Cond p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public CondElse visit(CondElse p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public While visit(While p, EnvCompiler env) {
            // TODO:
            return null;
        }

        public SExp visit(SExp p, EnvCompiler env) {
            // TODO:
            return null;
        }
    }

    public static class ItemVisitor implements Item.Visitor<Item, EnvCompiler> {
        public NoInit visit(NoInit p, EnvCompiler env) {
            return p;
        }

        public Init visit(Init p, EnvCompiler env) {
            // TODO:
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
