package fr.rthd.jlc.compiler;

import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Ass;
import javalette.Absyn.BStmt;
import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.EAdd;
import javalette.Absyn.EAnd;
import javalette.Absyn.EApp;
import javalette.Absyn.ELitDoub;
import javalette.Absyn.ELitFalse;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ELitTrue;
import javalette.Absyn.EMul;
import javalette.Absyn.EOr;
import javalette.Absyn.ERel;
import javalette.Absyn.EString;
import javalette.Absyn.EVar;
import javalette.Absyn.Empty;
import javalette.Absyn.Expr;
import javalette.Absyn.FnDef;
import javalette.Absyn.Incr;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.ListExpr;
import javalette.Absyn.ListItem;
import javalette.Absyn.ListStmt;
import javalette.Absyn.ListTopDef;
import javalette.Absyn.Neg;
import javalette.Absyn.NoInit;
import javalette.Absyn.Not;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.Ret;
import javalette.Absyn.SExp;
import javalette.Absyn.Stmt;
import javalette.Absyn.TopDef;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

public class LLVMCompiler extends Compiler {
    @Override
    public String compile(Prog p, Env<?, FunType> parent) {
        EnvCompiler env = new EnvCompiler(parent);
        p.accept(new ProgVisitor(), null);
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
            FunType funType = env.lookupFun(p.ident_);



            return null;
        }
    }

    public static class ExprVisitor implements Expr.Visitor<Expr, EnvCompiler> {
        public EVar visit(EVar p, EnvCompiler env) {
            return p;
        }

        public ELitInt visit(ELitInt p, EnvCompiler env) {
            return p;
        }

        public ELitDoub visit(ELitDoub p, EnvCompiler env) {
            return p;
        }

        public ELitTrue visit(ELitTrue p, EnvCompiler env) {
            return p;
        }

        public ELitFalse visit(ELitFalse p, EnvCompiler env) {
            return p;
        }

        public EApp visit(EApp p, EnvCompiler env) {
            ListExpr expr = new ListExpr();
            for (Expr x : p.listexpr_) {
                expr.add(x.accept(new ExprVisitor(), null));
            }
            return new EApp(p.ident_, expr);
        }

        public EString visit(EString p, EnvCompiler env) {
            return p;
        }

        public Neg visit(Neg p, EnvCompiler env) {
            return new Neg(p.expr_.accept(new ExprVisitor(), null));
        }

        public Not visit(Not p, EnvCompiler env) {
            return new Not(p.expr_.accept(new ExprVisitor(), null));
        }

        public EMul visit(EMul p, EnvCompiler env) {
            return new EMul(
                p.expr_1.accept(new ExprVisitor(), null),
                p.mulop_,
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EAdd visit(EAdd p, EnvCompiler env) {
            return new EAdd(
                p.expr_1.accept(new ExprVisitor(), null),
                p.addop_,
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public ERel visit(ERel p, EnvCompiler env) {
            return new ERel(
                p.expr_1.accept(new ExprVisitor(), null),
                p.relop_,
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EAnd visit(EAnd p, EnvCompiler env) {
            return new EAnd(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EOr visit(EOr p, EnvCompiler env) {
            return new EOr(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            );
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
            ListItem items = new ListItem();
            for (Item x : p.listitem_) {
                items.add(x.accept(new ItemVisitor(), null));
            }

            return new Decl(p.type_, items);
        }

        public Ass visit(Ass p, EnvCompiler env) {
            return new Ass(
                p.ident_,
                p.expr_.accept(new ExprVisitor(), null)
            );
        }

        public Incr visit(Incr p, EnvCompiler env) {
            return p;
        }

        public Decr visit(Decr p, EnvCompiler env) {
            return p;
        }

        public Ret visit(Ret p, EnvCompiler env) {
            return new Ret(p.expr_.accept(new ExprVisitor(), null));
        }

        public VRet visit(VRet p, EnvCompiler env) {
            return p;
        }

        public Cond visit(Cond p, EnvCompiler env) {
            return new Cond(
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_.accept(new StmtVisitor(), null)
            );
        }

        public CondElse visit(CondElse p, EnvCompiler env) {
            return new CondElse(
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_1.accept(new StmtVisitor(), null),
                p.stmt_2.accept(new StmtVisitor(), null)
            );
        }

        public While visit(While p, EnvCompiler env) {
            return new While(
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_.accept(new StmtVisitor(), null)
            );
        }

        public SExp visit(SExp p, EnvCompiler env) {
            return new SExp(p.expr_.accept(new ExprVisitor(), null));
        }
    }

    public static class ItemVisitor implements Item.Visitor<Item, EnvCompiler> {
        public NoInit visit(NoInit p, EnvCompiler env) {
            return p;
        }

        public Init visit(Init p, EnvCompiler env) {
            return new Init(p.ident_, p.expr_.accept(new ExprVisitor(), null));
        }
    }
}
