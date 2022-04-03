package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.Choice;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunArg;
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
import javalette.Absyn.ListExpr;
import javalette.Absyn.ListItem;
import javalette.Absyn.ListStmt;
import javalette.Absyn.ListTopDef;
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

public class Optimizer {
    private static Expr operatorAction(
        AnnotatedExpr<?> left,
        AnnotatedExpr<?> right,
        OperatorAction<Integer> onInt,
        OperatorAction<Double> onDouble,
        OperatorAction<Boolean> onBool,
        OperatorAction<Expr> onDefault
    ) {
        if (left.parentExp instanceof ELitInt &&
            right.parentExp instanceof ELitInt) {
            int lvalue = ((ELitInt) left.parentExp).integer_;
            int rvalue = ((ELitInt) right.parentExp).integer_;
            return onInt.execute(lvalue, rvalue);
        } else if (left.parentExp instanceof ELitDoub &&
                   right.parentExp instanceof ELitDoub) {
            double lvalue = ((ELitDoub) left.parentExp).double_;
            double rvalue = ((ELitDoub) right.parentExp).double_;
            return onDouble.execute(lvalue, rvalue);
        } else {
            Boolean lvalue = null;
            Boolean rvalue = null;

            if (left.parentExp instanceof ELitTrue) {
                lvalue = true;
            } else if (left.parentExp instanceof ELitFalse) {
                lvalue = false;
            }

            if (right.parentExp instanceof ELitTrue) {
                rvalue = true;
            } else if (right.parentExp instanceof ELitFalse) {
                rvalue = false;
            }

            if (lvalue == null || rvalue == null) {
                return onDefault.execute(left, right);
            } else {
                return onBool.execute(lvalue, rvalue);
            }
        }
    }

    public Prog optimize(Prog p, Env<?, FunType> parentEnv) {
        EnvOptimizer env = new EnvOptimizer(parentEnv);
        // First pass will mark functions as pure or impure
        p = p.accept(new ProgVisitor(), env);
        env.newPass();
        // Second pass will optimize expressions based on functions purity
        return p.accept(new ProgVisitor(), env);
    }

    private interface OperatorAction<T> {
        Expr execute(T lvalue, T rvalue);
    }

    public static class ProgVisitor implements Prog.Visitor<Prog, EnvOptimizer> {
        public Program visit(Program p, EnvOptimizer env) {
            ListTopDef topDef = new ListTopDef();

            for (TopDef def : p.listtopdef_) {
                topDef.add(def.accept(new TopDefVisitor(), env));
            }

            ListTopDef usedTopDef = new ListTopDef();

            for (TopDef def : topDef) {
                FunTypeOptimizer func = env.lookupFun(((FnDef) def).ident_);
                if (func.isUsedByMain()) {
                    func.updatePurity();
                    usedTopDef.add(def);
                } else {
                    env.removeFun(func.name);
                }
            }

            for (TopDef def : usedTopDef) {
                FunTypeOptimizer func = env.lookupFun(((FnDef) def).ident_);
                func.clearUsage();
            }

            return new Program(usedTopDef);
        }
    }

    public static class TopDefVisitor implements TopDef.Visitor<TopDef, EnvOptimizer> {
        public FnDef visit(FnDef f, EnvOptimizer env) {
            FunTypeOptimizer func = env.lookupFun(f.ident_);
            env.setCurrentFunction(func);

            env.enterScope();

            for (FunArg arg : func.args) {
                env.insertVar(
                    arg.name,
                    new AnnotatedExpr<>(arg.type, new EVar(arg.name))
                );
            }

            Blk nBlock = f.blk_.accept(new BlkVisitor(), env);

            env.leaveScope();

            return new FnDef(
                f.type_,
                f.ident_,
                f.listarg_,
                nBlock
            );
        }
    }

    public static class BlkVisitor implements Blk.Visitor<Blk, EnvOptimizer> {
        public Block visit(Block p, EnvOptimizer env) {
            ListStmt statements = new ListStmt();

            env.enterScope();

            for (Stmt s : p.liststmt_) {
                AnnotatedStmt<?> stmt = s.accept(new StmtVisitor(), env);
                if (stmt.parentStmt instanceof Empty) {
                    continue;
                }

                statements.add(stmt);

                if (stmt.doesReturn()) {
                    break;
                }
            }

            env.leaveScope();

            return new Block(statements);
        }
    }

    public static class StmtVisitor implements Stmt.Visitor<AnnotatedStmt<? extends Stmt>, EnvOptimizer> {
        public AnnotatedStmt<Empty> visit(Empty s, EnvOptimizer env) {
            return new AnnotatedStmt<>(new Empty());
        }

        public AnnotatedStmt<Stmt> visit(BStmt s, EnvOptimizer env) {
            Block blk = (Block) s.blk_.accept(new BlkVisitor(), env);
            if (blk.liststmt_.size() == 0) {
                return new AnnotatedStmt<>(new Empty());
            }

            // Not empty because of above, and shouldn't contain a returning
            //  statement if the last one isn't a returning statement as well,
            //  otherwise it means we didn't do a clean cut
            boolean doesReturn = (
                (AnnotatedStmt<?>) blk.liststmt_.get(blk.liststmt_.size() - 1)
            ).doesReturn();

            return new AnnotatedStmt<>(new BStmt(blk), doesReturn);
        }

        public AnnotatedStmt<Decl> visit(Decl s, EnvOptimizer env) {
            TypeCode type = s.type_.accept(new TypeVisitor(), null);
            ListItem items = new ListItem();

            for (Item item : s.listitem_) {
                items.add(item.accept(new ItemVisitor(type), env));
            }

            return new AnnotatedStmt<>(new Decl(s.type_, items));
        }

        public AnnotatedStmt<Ass> visit(Ass s, EnvOptimizer env) {
            return new AnnotatedStmt<>(new Ass(s.ident_, s.expr_.accept(
                new ExprVisitor(),
                env
            )));
        }

        public AnnotatedStmt<Incr> visit(Incr s, EnvOptimizer env) {
            return new AnnotatedStmt<>(new Incr(s.ident_));
        }

        public AnnotatedStmt<Decr> visit(Decr s, EnvOptimizer env) {
            return new AnnotatedStmt<>(new Decr(s.ident_));
        }

        public AnnotatedStmt<Ret> visit(Ret s, EnvOptimizer env) {
            return new AnnotatedStmt<>(new Ret(s.expr_.accept(
                new ExprVisitor(),
                env
            )), true);
        }

        public AnnotatedStmt<VRet> visit(VRet s, EnvOptimizer env) {
            return new AnnotatedStmt<>(new VRet(), true);
        }

        public AnnotatedStmt<?> visit(Cond s, EnvOptimizer env) {
            AnnotatedExpr<?> exp = s.expr_.accept(
                new ExprVisitor(),
                env
            );

            if (exp.parentExp instanceof ELitTrue) {
                return s.stmt_.accept(new StmtVisitor(), env);
            } else if (exp.parentExp instanceof ELitFalse) {
                return new AnnotatedStmt<>(new Empty());
            } else {
                env.enterScope();
                Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
                env.leaveScope();

                return new AnnotatedStmt<>(new Cond(exp, stmt));
            }
        }

        public AnnotatedStmt<?> visit(CondElse s, EnvOptimizer env) {
            AnnotatedExpr<?> exp = s.expr_.accept(
                new ExprVisitor(),
                env
            );

            if (exp.parentExp instanceof ELitTrue) {
                return s.stmt_1.accept(new StmtVisitor(), env);
            } else if (exp.parentExp instanceof ELitFalse) {
                return s.stmt_2.accept(new StmtVisitor(), env);
            } else {
                env.enterScope();
                AnnotatedStmt<?> stmt1 = s.stmt_1.accept(
                    new StmtVisitor(),
                    env
                );
                env.leaveScope();

                env.enterScope();
                AnnotatedStmt<?> stmt2 = s.stmt_2.accept(
                    new StmtVisitor(),
                    env
                );
                env.leaveScope();

                return new AnnotatedStmt<>(
                    new CondElse(exp, stmt1, stmt2),
                    stmt1.doesReturn() && stmt2.doesReturn()
                );
            }
        }

        public AnnotatedStmt<?> visit(While s, EnvOptimizer env) {
            AnnotatedExpr<?> exp = s.expr_.accept(
                new ExprVisitor(),
                env
            );

            // TODO: Optimize infinite loop

            if (exp.parentExp instanceof ELitFalse) {
                return new AnnotatedStmt<>(new Empty());
            } else {
                env.enterScope();
                Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
                env.leaveScope();

                if (exp.parentExp instanceof ELitTrue) {
                    // Functions with infinite loops cannot safely be marked
                    //  as pure for the sake of optimization, so we can simply
                    //  remove functions calls that are known to be pure
                    env.getCurrentFunction().setPure(Choice.FALSE);
                }

                return new AnnotatedStmt<>(
                    new While(exp, stmt),
                    exp.parentExp instanceof ELitTrue
                );
            }
        }

        public AnnotatedStmt<?> visit(SExp s, EnvOptimizer env) {
            AnnotatedExpr<?> expr = (AnnotatedExpr<?>) s.expr_;
            if (expr.parentExp instanceof EApp) {
                FunTypeOptimizer funType = env.lookupFun(
                    ((EApp) expr.parentExp).ident_
                );
                if (funType.isPure() == Choice.TRUE) {
                    return new AnnotatedStmt<>(new Empty());
                }
            }
            expr = s.expr_.accept(
                new ExprVisitor(),
                env
            );
            return new AnnotatedStmt<>(new SExp(expr));
        }
    }

    public static class ItemVisitor implements Item.Visitor<Item, EnvOptimizer> {
        private final TypeCode varType;

        public ItemVisitor(TypeCode varType) {
            this.varType = varType;
        }

        public NoInit visit(NoInit s, EnvOptimizer env) {
            env.insertVar(
                s.ident_,
                new AnnotatedExpr<>(varType, new EVar(s.ident_))
            );
            return new NoInit(s.ident_);
        }

        public Init visit(Init s, EnvOptimizer env) {
            // FIXME: Should it be evaluated before or after inserting var?
            // env.insertVar(s.ident_, null);
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            // TODO: Keep track of const variables
            // env.insertVar(s.ident_, exp);
            env.insertVar(
                s.ident_,
                new AnnotatedExpr<>(exp.type, new EVar(s.ident_))
            );

            return new Init(s.ident_, exp);
        }
    }

    public static class ExprVisitor implements Expr.Visitor<AnnotatedExpr<? extends Expr>, EnvOptimizer> {
        public AnnotatedExpr<?> visit(EVar e, EnvOptimizer env) {
            AnnotatedExpr<?> expr = env.lookupVar(e.ident_);
            assert expr != null;
            return expr;
        }

        public AnnotatedExpr<ELitInt> visit(ELitInt e, EnvOptimizer env) {
            return new AnnotatedExpr<>(TypeCode.CInt, e);
        }

        public AnnotatedExpr<ELitDoub> visit(ELitDoub e, EnvOptimizer env) {
            return new AnnotatedExpr<>(TypeCode.CDouble, e);
        }

        public AnnotatedExpr<ELitTrue> visit(ELitTrue e, EnvOptimizer env) {
            return new AnnotatedExpr<>(TypeCode.CBool, e);
        }

        public AnnotatedExpr<ELitFalse> visit(ELitFalse e, EnvOptimizer env) {
            return new AnnotatedExpr<>(TypeCode.CBool, e);
        }

        public AnnotatedExpr<EApp> visit(EApp e, EnvOptimizer env) {
            FunTypeOptimizer funcType = env.lookupFun(e.ident_);
            funcType.addUsageIn(env.getCurrentFunction());

            ListExpr exps = new ListExpr();
            for (int i = 0; i < funcType.args.size(); ++i) {
                AnnotatedExpr<?> exp = e.listexpr_.get(i).accept(
                    new ExprVisitor(),
                    env
                );
                exps.add(exp);
            }

            return new AnnotatedExpr<>(
                funcType.retType,
                new EApp(e.ident_, exps)
            );
        }

        public AnnotatedExpr<EString> visit(EString e, EnvOptimizer env) {
            return new AnnotatedExpr<>(TypeCode.CString, e);
        }

        public AnnotatedExpr<?> visit(Neg e, EnvOptimizer env) {
            AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
            if (expr.parentExp instanceof ELitInt) {
                return new AnnotatedExpr<>(
                    TypeCode.CInt,
                    new ELitInt(-((ELitInt) expr.parentExp).integer_)
                );
            } else if (expr.parentExp instanceof ELitDoub) {
                return new AnnotatedExpr<>(
                    TypeCode.CDouble,
                    new ELitDoub(-((ELitDoub) expr.parentExp).double_)
                );
            } else {
                return new AnnotatedExpr<>(
                    expr.type,
                    new Neg(expr.parentExp)
                );
            }
        }

        public AnnotatedExpr<?> visit(Not e, EnvOptimizer env) {
            AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
            if (expr.parentExp instanceof ELitTrue) {
                return new AnnotatedExpr<>(
                    TypeCode.CBool,
                    new ELitFalse()
                );
            } else if (expr.parentExp instanceof ELitFalse) {
                return new AnnotatedExpr<>(
                    TypeCode.CBool,
                    new ELitTrue()
                );
            } else {
                return new AnnotatedExpr<>(
                    expr.type,
                    new Not(expr.parentExp)
                );
            }
        }

        public AnnotatedExpr<?> visit(EMul e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return e.mulop_.accept(new MulOpVisitor(left, right), env);
        }

        public AnnotatedExpr<?> visit(EAdd e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return e.addop_.accept(new AddOpVisitor(left, right), env);
        }

        public AnnotatedExpr<?> visit(ERel e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return e.relop_.accept(new RelOpVisitor(left, right), env);
        }

        public AnnotatedExpr<?> visit(EAnd e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

            if (left.parentExp instanceof ELitTrue) {
                return new AnnotatedExpr<>(TypeCode.CBool, right);
            } else if (right.parentExp instanceof ELitTrue) {
                return new AnnotatedExpr<>(TypeCode.CBool, left);
            } else if (left.parentExp instanceof ELitFalse) {
                // Short circuit
                return new AnnotatedExpr<>(TypeCode.CBool, new ELitFalse());
            } else if (right.parentExp instanceof ELitFalse) {
                // Still need to execute the left expression, even though
                // we know it will result in literal false
                return new AnnotatedExpr<>(
                    TypeCode.CBool,
                    new EAnd(left, right)
                );
            } else {
                return new AnnotatedExpr<>(
                    TypeCode.CBool,
                    new EAnd(left, right)
                );
            }
        }

        public AnnotatedExpr<?> visit(EOr e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

            if (left.parentExp instanceof ELitTrue) {
                // Short circuit
                return new AnnotatedExpr<>(TypeCode.CBool, new ELitTrue());
            } else if (right.parentExp instanceof ELitTrue) {
                // Still need to execute the left expression, even though
                // we know it will result in literal true
                return new AnnotatedExpr<>(
                    TypeCode.CBool,
                    new EOr(left, right)
                );
            } else if (left.parentExp instanceof ELitFalse) {
                return new AnnotatedExpr<>(TypeCode.CBool, right);
            } else if (right.parentExp instanceof ELitFalse) {
                return new AnnotatedExpr<>(TypeCode.CBool, left);
            } else {
                return new AnnotatedExpr<>(
                    TypeCode.CBool,
                    new EOr(left, right)
                );
            }
        }
    }

    public static class AddOpVisitor implements AddOp.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
        private final AnnotatedExpr<?> left;
        private final AnnotatedExpr<?> right;

        public AddOpVisitor(AnnotatedExpr<?> left, AnnotatedExpr<?> right) {
            this.left = left;
            this.right = right;
        }

        public AnnotatedExpr<?> visit(Plus p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> new ELitInt(l + r),
                (l, r) -> new ELitDoub(l + r),
                null,
                (l, r) -> new EAdd(l, new Plus(), r)
            ));
        }

        public AnnotatedExpr<?> visit(Minus p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> new ELitInt(l - r),
                (l, r) -> new ELitDoub(l - r),
                null,
                (l, r) -> new EAdd(l, new Minus(), r)
            ));
        }
    }

    public static class MulOpVisitor implements MulOp.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
        private final AnnotatedExpr<?> left;
        private final AnnotatedExpr<?> right;

        public MulOpVisitor(AnnotatedExpr<?> left, AnnotatedExpr<?> right) {
            this.left = left;
            this.right = right;
        }

        public AnnotatedExpr<?> visit(Times p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> new ELitInt(l * r),
                (l, r) -> new ELitDoub(l * r),
                null,
                (l, r) -> new EMul(l, new Times(), r)
            ));
        }

        public AnnotatedExpr<?> visit(Div p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> new ELitInt(l / r),
                (l, r) -> new ELitDoub(l / r),
                null,
                (l, r) -> new EMul(l, new Div(), r)
            ));
        }

        public AnnotatedExpr<?> visit(Mod p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> new ELitInt(l % r),
                (l, r) -> new ELitDoub(l % r),
                null,
                (l, r) -> new EMul(l, new Mod(), r)
            ));
        }
    }

    public static class RelOpVisitor implements RelOp.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
        private final AnnotatedExpr<?> left;
        private final AnnotatedExpr<?> right;

        public RelOpVisitor(AnnotatedExpr<?> left, AnnotatedExpr<?> right) {
            this.left = left;
            this.right = right;
        }

        public AnnotatedExpr<?> visit(LTH p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> l < r ? new ELitTrue() : new ELitFalse(),
                (l, r) -> l < r ? new ELitTrue() : new ELitFalse(),
                null,
                (l, r) -> new ERel(l, new LTH(), r)
            ));
        }

        public AnnotatedExpr<?> visit(LE p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
                (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
                null,
                (l, r) -> new ERel(l, new LE(), r)
            ));
        }

        public AnnotatedExpr<?> visit(GTH p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> l > r ? new ELitTrue() : new ELitFalse(),
                (l, r) -> l > r ? new ELitTrue() : new ELitFalse(),
                null,
                (l, r) -> new ERel(l, new GTH(), r)
            ));
        }

        public AnnotatedExpr<?> visit(GE p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
                (l, r) -> l <= r ? new ELitTrue() : new ELitFalse(),
                null,
                (l, r) -> new ERel(l, new GE(), r)
            ));
        }

        public AnnotatedExpr<?> visit(EQU p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> l.equals(r) ? new ELitTrue() : new ELitFalse(),
                (l, r) -> l.equals(r) ? new ELitTrue() : new ELitFalse(),
                (l, r) -> l.equals(r) ? new ELitTrue() : new ELitFalse(),
                (l, r) -> new ERel(l, new EQU(), r)
            ));
        }

        public AnnotatedExpr<?> visit(NE p, EnvOptimizer env) {
            return new AnnotatedExpr<>(left.type, operatorAction(
                left,
                right,
                (l, r) -> l.equals(r) ? new ELitFalse() : new ELitTrue(),
                (l, r) -> l.equals(r) ? new ELitFalse() : new ELitTrue(),
                (l, r) -> l.equals(r) ? new ELitFalse() : new ELitTrue(),
                (l, r) -> new ERel(l, new NE(), r)
            ));
        }
    }
}
