package fr.rthd.jlc.internal;

import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Ass;
import javalette.Absyn.AttrMember;
import javalette.Absyn.BStmt;
import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
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
import javalette.Absyn.Empty;
import javalette.Absyn.Expr;
import javalette.Absyn.FnDef;
import javalette.Absyn.FnMember;
import javalette.Absyn.For;
import javalette.Absyn.FuncDef;
import javalette.Absyn.Incr;
import javalette.Absyn.Index;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.ListExpr;
import javalette.Absyn.ListIndex;
import javalette.Absyn.ListItem;
import javalette.Absyn.ListMember;
import javalette.Absyn.ListStmt;
import javalette.Absyn.ListTopDef;
import javalette.Absyn.Member;
import javalette.Absyn.Neg;
import javalette.Absyn.NoInit;
import javalette.Absyn.Not;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.Ret;
import javalette.Absyn.SExp;
import javalette.Absyn.SIndex;
import javalette.Absyn.Stmt;
import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import javalette.Absyn.VRet;
import javalette.Absyn.While;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Debug class to recreate an AST as expected by PrettyPrinter, without our
 * custom type annotations
 * @author RomainTHD
 */
@NonNls
public class Unannotater implements Visitor {
    @NotNull
    @Override
    public Prog accept(
        @NotNull Prog p,
        @NotNull Env<?, FunType, ClassType> ignored
    ) {
        return p.accept(new ProgVisitor(), null);
    }

    private static class ProgVisitor implements Prog.Visitor<Prog, Void> {
        public Program visit(Program p, Void ignored) {
            ListTopDef listtopdef = new ListTopDef();
            for (TopDef topdef : p.listtopdef_) {
                listtopdef.add(topdef.accept(new TopDefVisitor(), null));
            }
            return new Program(listtopdef);
        }
    }

    private static class TopDefVisitor implements TopDef.Visitor<TopDef, Void> {
        public TopFnDef visit(TopFnDef p, Void ignored) {
            return new TopFnDef(
                p.funcdef_.accept(new FuncDefVisitor(), null)
            );
        }

        public TopClsDef visit(TopClsDef p, Void ignored) {
            return new TopClsDef(
                p.classdef_.accept(new ClassDefVisitor(), null)
            );
        }
    }

    private static class FuncDefVisitor implements FuncDef.Visitor<FnDef, Void> {
        public FnDef visit(FnDef p, Void ignored) {
            return new FnDef(
                p.type_,
                p.ident_,
                p.listarg_,
                p.blk_.accept(new BlkVisitor(), null)
            );
        }
    }

    private static class ClassDefVisitor implements ClassDef.Visitor<ClassDef, Void> {
        public ClsDef visit(ClsDef p, Void ignored) {
            ListMember members = new ListMember();
            for (Member m : p.listmember_) {
                members.add(m.accept(new MemberVisitor(), null));
            }
            return new ClsDef(
                p.ident_,
                p.classinheritance_,
                members
            );
        }
    }

    private static class MemberVisitor implements Member.Visitor<Member, Void> {
        public FnMember visit(FnMember p, Void ignored) {
            return new FnMember(
                p.funcdef_.accept(new FuncDefVisitor(), null)
            );
        }

        public AttrMember visit(AttrMember p, Void ignored) {
            return p;
        }
    }

    private static class ExprVisitor implements Expr.Visitor<Expr, Void> {
        public ENull visit(ENull p, Void ignored) {
            return p;
        }

        public EVar visit(EVar p, Void ignored) {
            return p;
        }

        public ELitInt visit(ELitInt p, Void ignored) {
            return p;
        }

        public ELitDoub visit(ELitDoub p, Void ignored) {
            return p;
        }

        public ELitTrue visit(ELitTrue p, Void ignored) {
            return p;
        }

        public ELitFalse visit(ELitFalse p, Void ignored) {
            return p;
        }

        public EApp visit(EApp p, Void ignored) {
            ListExpr expr = new ListExpr();
            for (Expr x : p.listexpr_) {
                expr.add(x.accept(new ExprVisitor(), null));
            }
            return new EApp(p.expr_.accept(new ExprVisitor(), null), expr);
        }

        public EDot visit(EDot p, Void ignored) {
            return new EDot(
                p.expr_.accept(new ExprVisitor(), null),
                p.ident_
            );
        }

        public EIndex visit(EIndex p, Void ignored) {
            return new EIndex(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EString visit(EString p, Void ignored) {
            return p;
        }

        public ENew visit(ENew p, Void ignored) {
            ListIndex index = new ListIndex();
            for (Index x : p.listindex_) {
                index.add(x.accept(new IndexVisitor(), null));
            }
            return new ENew(
                p.basetype_,
                index
            );
        }

        public Neg visit(Neg p, Void ignored) {
            return new Neg(p.expr_.accept(new ExprVisitor(), null));
        }

        public Not visit(Not p, Void ignored) {
            return new Not(p.expr_.accept(new ExprVisitor(), null));
        }

        public EMul visit(EMul p, Void ignored) {
            return new EMul(
                p.expr_1.accept(new ExprVisitor(), null),
                p.mulop_,
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EAdd visit(EAdd p, Void ignored) {
            return new EAdd(
                p.expr_1.accept(new ExprVisitor(), null),
                p.addop_,
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public ERel visit(ERel p, Void ignored) {
            return new ERel(
                p.expr_1.accept(new ExprVisitor(), null),
                p.relop_,
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EAnd visit(EAnd p, Void ignored) {
            return new EAnd(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public EOr visit(EOr p, Void ignored) {
            return new EOr(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }
    }

    private static class BlkVisitor implements Blk.Visitor<Blk, Void> {
        public Block visit(Block p, Void ignored) {
            ListStmt stmt = new ListStmt();
            for (Stmt s : p.liststmt_) {
                stmt.add(s.accept(new StmtVisitor(), null));
            }
            return new Block(stmt);
        }
    }

    private static class IndexVisitor implements Index.Visitor<Index, Void> {
        public SIndex visit(SIndex p, Void ignored) {
            return new SIndex(p.expr_.accept(new ExprVisitor(), null));
        }
    }

    private static class StmtVisitor implements Stmt.Visitor<Stmt, Void> {
        public Empty visit(Empty p, Void ignored) {
            return p;
        }

        public BStmt visit(BStmt p, Void ignored) {
            return new BStmt(p.blk_.accept(new BlkVisitor(), null));
        }

        public Decl visit(Decl p, Void ignored) {
            ListItem items = new ListItem();
            for (Item x : p.listitem_) {
                items.add(x.accept(new ItemVisitor(), null));
            }

            return new Decl(p.type_, items);
        }

        public Ass visit(Ass p, Void ignored) {
            return new Ass(
                p.expr_1.accept(new ExprVisitor(), null),
                p.expr_2.accept(new ExprVisitor(), null)
            );
        }

        public Incr visit(Incr p, Void ignored) {
            return p;
        }

        public Decr visit(Decr p, Void ignored) {
            return p;
        }

        public Ret visit(Ret p, Void ignored) {
            return new Ret(p.expr_.accept(new ExprVisitor(), null));
        }

        public VRet visit(VRet p, Void ignored) {
            return p;
        }

        public Cond visit(Cond p, Void ignored) {
            return new Cond(
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_.accept(new StmtVisitor(), null)
            );
        }

        public CondElse visit(CondElse p, Void ignored) {
            return new CondElse(
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_1.accept(new StmtVisitor(), null),
                p.stmt_2.accept(new StmtVisitor(), null)
            );
        }

        public While visit(While p, Void ignored) {
            return new While(
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_.accept(new StmtVisitor(), null)
            );
        }

        public For visit(For p, Void ignored) {
            return new For(
                p.type_,
                p.ident_,
                p.expr_.accept(new ExprVisitor(), null),
                p.stmt_.accept(new StmtVisitor(), null)
            );
        }

        public SExp visit(SExp p, Void ignored) {
            return new SExp(p.expr_.accept(new ExprVisitor(), null));
        }
    }

    private static class ItemVisitor implements Item.Visitor<Item, Void> {
        public NoInit visit(NoInit p, Void ignored) {
            return p;
        }

        public Init visit(Init p, Void ignored) {
            return new Init(p.ident_, p.expr_.accept(new ExprVisitor(), null));
        }
    }
}
