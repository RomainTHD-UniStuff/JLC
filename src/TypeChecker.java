import javalette.Absyn.AddOp;
import javalette.Absyn.Arg;
import javalette.Absyn.Blk;
import javalette.Absyn.Expr;
import javalette.Absyn.Item;
import javalette.Absyn.MulOp;
import javalette.Absyn.Prog;
import javalette.Absyn.RelOp;
import javalette.Absyn.Stmt;
import javalette.Absyn.TopDef;
import javalette.Absyn.Type;

public class TypeChecker {
    public Prog typecheck(Prog p) {
        return p;
    }

    public static class ProgVisitor<R, A> implements Prog.Visitor<R, A> {
        public R visit(javalette.Absyn.Program p, A arg) {
            /* Code For Program Goes Here */
            for (TopDef x : p.listtopdef_) { /* ... */ }
            return null;
        }
    }

    public static class TopDefVisitor<R, A> implements TopDef.Visitor<R, A> {
        public R visit(javalette.Absyn.FnDef p, A arg) {
            /* Code For FnDef Goes Here */
            p.type_.accept(new TypeVisitor<R, A>(), arg);
            //p.ident_;
            for (Arg x : p.listarg_) { /* ... */ }
            p.blk_.accept(new BlkVisitor<R, A>(), arg);
            return null;
        }
    }

    public static class ArgVisitor<R, A> implements Arg.Visitor<R, A> {
        public R visit(javalette.Absyn.Argument p, A arg) {
            /* Code For Argument Goes Here */
            p.type_.accept(new TypeVisitor<R, A>(), arg);
            //p.ident_;
            return null;
        }
    }

    public static class BlkVisitor<R, A> implements Blk.Visitor<R, A> {
        public R visit(javalette.Absyn.Block p, A arg) {
            /* Code For Block Goes Here */
            for (Stmt x : p.liststmt_) { /* ... */ }
            return null;
        }
    }

    public static class StmtVisitor<R, A> implements Stmt.Visitor<R, A> {
        public R visit(javalette.Absyn.Empty p, A arg) {
            /* Code For Empty Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.BStmt p, A arg) {
            /* Code For BStmt Goes Here */
            p.blk_.accept(new BlkVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.Decl p, A arg) {
            /* Code For Decl Goes Here */
            p.type_.accept(new TypeVisitor<R, A>(), arg);
            for (Item x : p.listitem_) { /* ... */ }
            return null;
        }

        public R visit(javalette.Absyn.Ass p, A arg) {
            /* Code For Ass Goes Here */
            //p.ident_;
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.Incr p, A arg) {
            /* Code For Incr Goes Here */
            //p.ident_;
            return null;
        }

        public R visit(javalette.Absyn.Decr p, A arg) {
            /* Code For Decr Goes Here */
            //p.ident_;
            return null;
        }

        public R visit(javalette.Absyn.Ret p, A arg) {
            /* Code For Ret Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.VRet p, A arg) {
            /* Code For VRet Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Cond p, A arg) {
            /* Code For Cond Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            p.stmt_.accept(new StmtVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.CondElse p, A arg) {
            /* Code For CondElse Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            p.stmt_1.accept(new StmtVisitor<R, A>(), arg);
            p.stmt_2.accept(new StmtVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.While p, A arg) {
            /* Code For While Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            p.stmt_.accept(new StmtVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.SExp p, A arg) {
            /* Code For SExp Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }
    }

    public static class ItemVisitor<R, A> implements Item.Visitor<R, A> {
        public R visit(javalette.Absyn.NoInit p, A arg) {
            /* Code For NoInit Goes Here */
            //p.ident_;
            return null;
        }

        public R visit(javalette.Absyn.Init p, A arg) {
            /* Code For Init Goes Here */
            //p.ident_;
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }
    }

    public static class TypeVisitor<R, A> implements Type.Visitor<R, A> {
        public R visit(javalette.Absyn.Int p, A arg) {
            /* Code For Int Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Doub p, A arg) {
            /* Code For Doub Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Bool p, A arg) {
            /* Code For Bool Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Void p, A arg) {
            /* Code For Void Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Fun p, A arg) {
            /* Code For Fun Goes Here */
            p.type_.accept(new TypeVisitor<R, A>(), arg);
            for (Type x : p.listtype_) { /* ... */ }
            return null;
        }
    }

    public static class ExprVisitor<R, A> implements Expr.Visitor<R, A> {
        public R visit(javalette.Absyn.EVar p, A arg) {
            /* Code For EVar Goes Here */
            //p.ident_;
            return null;
        }

        public R visit(javalette.Absyn.ELitInt p, A arg) {
            /* Code For ELitInt Goes Here */
            //p.integer_;
            return null;
        }

        public R visit(javalette.Absyn.ELitDoub p, A arg) {
            /* Code For ELitDoub Goes Here */
            //p.double_;
            return null;
        }

        public R visit(javalette.Absyn.ELitTrue p, A arg) {
            /* Code For ELitTrue Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.ELitFalse p, A arg) {
            /* Code For ELitFalse Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.EApp p, A arg) {
            /* Code For EApp Goes Here */
            //p.ident_;
            for (Expr x : p.listexpr_) { /* ... */ }
            return null;
        }

        public R visit(javalette.Absyn.EString p, A arg) {
            /* Code For EString Goes Here */
            //p.string_;
            return null;
        }

        public R visit(javalette.Absyn.Neg p, A arg) {
            /* Code For Neg Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.Not p, A arg) {
            /* Code For Not Goes Here */
            p.expr_.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.EMul p, A arg) {
            /* Code For EMul Goes Here */
            p.expr_1.accept(new ExprVisitor<R, A>(), arg);
            p.mulop_.accept(new MulOpVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.EAdd p, A arg) {
            /* Code For EAdd Goes Here */
            p.expr_1.accept(new ExprVisitor<R, A>(), arg);
            p.addop_.accept(new AddOpVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.ERel p, A arg) {
            /* Code For ERel Goes Here */
            p.expr_1.accept(new ExprVisitor<R, A>(), arg);
            p.relop_.accept(new RelOpVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.EAnd p, A arg) {
            /* Code For EAnd Goes Here */
            p.expr_1.accept(new ExprVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }

        public R visit(javalette.Absyn.EOr p, A arg) {
            /* Code For EOr Goes Here */
            p.expr_1.accept(new ExprVisitor<R, A>(), arg);
            p.expr_2.accept(new ExprVisitor<R, A>(), arg);
            return null;
        }
    }

    public static class AddOpVisitor<R, A> implements AddOp.Visitor<R, A> {
        public R visit(javalette.Absyn.Plus p, A arg) {
            /* Code For Plus Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Minus p, A arg) {
            /* Code For Minus Goes Here */
            return null;
        }
    }

    public static class MulOpVisitor<R, A> implements MulOp.Visitor<R, A> {
        public R visit(javalette.Absyn.Times p, A arg) {
            /* Code For Times Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Div p, A arg) {
            /* Code For Div Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.Mod p, A arg) {
            /* Code For Mod Goes Here */
            return null;
        }
    }

    public static class RelOpVisitor<R, A> implements RelOp.Visitor<R, A> {
        public R visit(javalette.Absyn.LTH p, A arg) {
            /* Code For LTH Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.LE p, A arg) {
            /* Code For LE Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.GTH p, A arg) {
            /* Code For GTH Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.GE p, A arg) {
            /* Code For GE Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.EQU p, A arg) {
            /* Code For EQU Goes Here */
            return null;
        }

        public R visit(javalette.Absyn.NE p, A arg) {
            /* Code For NE Goes Here */
            return null;
        }
    }
}
