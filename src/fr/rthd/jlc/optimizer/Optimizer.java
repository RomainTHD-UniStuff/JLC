package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.AddOp;
import javalette.Absyn.Ass;
import javalette.Absyn.BStmt;
import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.Bool;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.Div;
import javalette.Absyn.Doub;
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
import javalette.Absyn.Fun;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.Incr;
import javalette.Absyn.Init;
import javalette.Absyn.Int;
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
import javalette.Absyn.Type;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

import java.util.Arrays;

public class Optimizer {
    public Prog optimize(Prog p) {
        EnvOptimizer env = new EnvOptimizer();
        return p.accept(new ProgVisitor(), env);
    }

    public static class ProgVisitor implements Prog.Visitor<Prog, EnvOptimizer> {
        public Program visit(Program p, EnvOptimizer env) {
            ListTopDef topDef = new ListTopDef();

            for (TopDef def : p.listtopdef_) {
                topDef.add(def.accept(new TopDefVisitor(), env));
            }

            return new Program(topDef);
        }
    }

    public static class TopDefVisitor implements TopDef.Visitor<TopDef, EnvOptimizer> {
        public FnDef visit(FnDef f, EnvOptimizer env) {
            FunType func = env.lookupFun(f.ident_);

            env.enterScope();

            for (FunArg arg : func.args) {
                env.insertVar(arg.name, new AnnotatedExpr<>(arg.type, new EVar(arg.name)));
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
                Stmt stmt = s.accept(new StmtVisitor(), env);
                if (stmt instanceof Empty) {
                    continue;
                }

                statements.add(stmt);

                if (stmt instanceof Ret || stmt instanceof VRet) {
                    break;
                }
            }

            env.leaveScope();

            return new Block(statements);
        }
    }

    public static class StmtVisitor implements Stmt.Visitor<Stmt, EnvOptimizer> {
        public Empty visit(Empty s, EnvOptimizer env) {
            return new Empty();
        }

        public Stmt visit(BStmt s, EnvOptimizer env) {
            Blk blk = s.blk_.accept(new BlkVisitor(), env);
            if (blk instanceof Block) {
                if (((Block) blk).liststmt_.size() == 0) {
                    return new Empty();
                }
            }

            return new BStmt(blk);
        }

        public Decl visit(Decl s, EnvOptimizer env) {
            TypeCode type = s.type_.accept(new TypeVisitor(), null);
            ListItem items = new ListItem();

            for (Item item : s.listitem_) {
                items.add(item.accept(new ItemVisitor(), new Object[]{env, type}));
            }

            return new Decl(s.type_, items);
        }

        public Ass visit(Ass s, EnvOptimizer env) {
            return new Ass(s.ident_, s.expr_.accept(
                new ExprVisitor((AnnotatedExpr<?>) s.expr_),
                env
            ));
        }

        public Incr visit(Incr s, EnvOptimizer env) {
            return new Incr(s.ident_);
        }

        public Decr visit(Decr s, EnvOptimizer env) {
            return new Decr(s.ident_);
        }

        public Ret visit(Ret s, EnvOptimizer env) {
            return new Ret(s.expr_.accept(
                new ExprVisitor((AnnotatedExpr<?>) s.expr_),
                env
            ));
        }

        public VRet visit(VRet s, EnvOptimizer env) {
            return new VRet();
        }

        public Stmt visit(Cond s, EnvOptimizer env) {
            AnnotatedExpr<?> exp = s.expr_.accept(
                new ExprVisitor((AnnotatedExpr<?>) s.expr_),
                env
            );

            if (exp.parentExp instanceof ELitTrue) {
                return s.stmt_.accept(new StmtVisitor(), env);
            } else if (exp.parentExp instanceof ELitFalse) {
                return new Empty();
            } else {
                env.enterScope();
                Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
                env.leaveScope();

                return new Cond(exp, stmt);
            }
        }

        public Stmt visit(CondElse s, EnvOptimizer env) {
            AnnotatedExpr<?> exp = s.expr_.accept(
                new ExprVisitor((AnnotatedExpr<?>) s.expr_),
                env
            );

            if (exp.parentExp instanceof ELitTrue) {
                return s.stmt_1.accept(new StmtVisitor(), env);
            } else if (exp.parentExp instanceof ELitFalse) {
                return s.stmt_2.accept(new StmtVisitor(), env);
            } else {
                env.enterScope();
                Stmt stmt1 = s.stmt_1.accept(new StmtVisitor(), env);
                env.leaveScope();

                env.enterScope();
                Stmt stmt2 = s.stmt_2.accept(new StmtVisitor(), env);
                env.leaveScope();

                return new CondElse(exp, stmt1, stmt2);
            }
        }

        public Stmt visit(While s, EnvOptimizer env) {
            AnnotatedExpr<?> exp = s.expr_.accept(
                new ExprVisitor((AnnotatedExpr<?>) s.expr_),
                env
            );

            // TODO: Optimize infinite loop

            if (exp.parentExp instanceof ELitFalse) {
                return new Empty();
            } else {
                env.enterScope();
                Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
                env.leaveScope();

                return new While(exp, stmt);
            }
        }

        public SExp visit(SExp s, EnvOptimizer env) {
            AnnotatedExpr<?> expr = s.expr_.accept(
                new ExprVisitor((AnnotatedExpr<?>) s.expr_),
                env
            );
            return new SExp(expr);
        }
    }

    public static class ItemVisitor implements Item.Visitor<Item, Object[]> {
        public NoInit visit(NoInit s, Object[] args) {
            EnvOptimizer env = (EnvOptimizer) args[0];
            TypeCode varType = (TypeCode) args[1];
            env.insertVar(s.ident_, new AnnotatedExpr<>(varType, new EVar(s.ident_)));
            return new NoInit(s.ident_);
        }

        public Init visit(Init s, Object[] args) {
            EnvOptimizer env = (EnvOptimizer) args[0];

            // FIXME: Should it be evaluated before or after inserting var?
            env.insertVar(s.ident_, null);
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor((AnnotatedExpr<?>) s.expr_), env);
            env.updateVar(s.ident_, exp);

            return new Init(s.ident_, exp);
        }
    }

    public static class TypeVisitor implements Type.Visitor<TypeCode, Void> {
        public TypeCode visit(Bool t, Void ignored) {
            return TypeCode.CBool;
        }

        public TypeCode visit(Int t, Void ignored) {
            return TypeCode.CInt;
        }

        public TypeCode visit(Doub t, Void ignored) {
            return TypeCode.CDouble;
        }

        public TypeCode visit(javalette.Absyn.Void t, Void ignored) {
            return TypeCode.CVoid;
        }

        public TypeCode visit(Fun p, Void ignored) {
            throw new UnsupportedOperationException("visit(javalette.Absyn.Fun)");
        }
    }

    public static class ExprVisitor implements Expr.Visitor<AnnotatedExpr<?>, EnvOptimizer> {
        private final AnnotatedExpr<?> annotated;

        public ExprVisitor(AnnotatedExpr<?> annotated) {
            this.annotated = annotated;
        }

        public AnnotatedExpr<?> visit(EVar e, EnvOptimizer env) {
            return env.lookupVar(e.ident_);
        }

        public AnnotatedExpr<?> visit(ELitInt e, EnvOptimizer env) {
            return annotated;
        }

        public AnnotatedExpr<?> visit(ELitDoub e, EnvOptimizer env) {
            return annotated;
        }

        public AnnotatedExpr<?> visit(ELitTrue e, EnvOptimizer env) {
            return annotated;
        }

        public AnnotatedExpr<?> visit(ELitFalse e, EnvOptimizer env) {
            return annotated;
        }

        public AnnotatedExpr<EApp> visit(EApp e, EnvOptimizer env) {
            FunType funcType = env.lookupFun(e.ident_);

            ListExpr exps = new ListExpr();
            for (int i = 0; i < funcType.args.size(); ++i) {
                AnnotatedExpr<?> exp = e.listexpr_.get(i).accept(new ExprVisitor(), env);
                exps.add(exp);
            }

            return new AnnotatedExpr<>(funcType.retType, new EApp(e.ident_, exps));
        }

        public AnnotatedExpr<?> visit(EString e, EnvOptimizer env) {
            return annotated;
        }

        public AnnotatedExpr<?> visit(Neg e, EnvOptimizer env) {
            AnnotatedExpr<?> opt = annotated.accept(new ExprVisitor(annotated), env);
            if (opt.parentExp instanceof ELitInt) {
                return new AnnotatedExpr<>(
                    annotated.type,
                    new ELitInt(-((ELitInt) opt.parentExp).integer_)
                );
            } else if (opt.parentExp instanceof ELitDoub) {
                return new AnnotatedExpr<>(
                    annotated.type,
                    new ELitDoub(-((ELitDoub) opt.parentExp).double_)
                );
            } else {
                return annotated;
            }
        }

        public AnnotatedExpr<?> visit(Not e, EnvOptimizer env) {
            if (annotated.parentExp instanceof ELitTrue) {
                return new AnnotatedExpr<>(
                    annotated.type,
                    new ELitFalse()
                );
            } else if (annotated.parentExp instanceof ELitFalse) {
                return new AnnotatedExpr<>(
                    annotated.type,
                    new ELitTrue()
                );
            } else {
                return annotated;
            }
        }

        public AnnotatedExpr<EMul> visit(EMul e, EnvOptimizer env) {
            AnnotatedExpr<?> expr = annotated.accept(new ExprVisitor(annotated), env);
            AnnotatedExpr<?> left = annotated.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = (AnnotatedExpr<?>) (((EMul) annotated.parentExp).expr_2);
            return e.mulop_.accept(new MulOpVisitor(left, right), env);
        }

        public AnnotatedExpr<EAdd> visit(EAdd e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return new AnnotatedExpr<>(
                left.type,
                new EAdd(
                    left,
                    e.addop_,
                    right
                )
            );
        }

        public AnnotatedExpr<ERel> visit(ERel e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new ERel(
                    left,
                    e.relop_,
                    right
                )
            );
        }

        public AnnotatedExpr<EAnd> visit(EAnd e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EAnd(
                    left,
                    right
                )
            );
        }

        public AnnotatedExpr<EOr> visit(EOr e, EnvOptimizer env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EOr(
                    left,
                    right
                )
            );
        }
    }

    public static class AddOpVisitor implements AddOp.Visitor<String, Void> {
        public String visit(Plus p, Void ignored) {
            return "addition";
        }

        public String visit(Minus p, Void ignored) {
            return "subtraction";
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
            if (left.parentExp instanceof ELitInt && right.parentExp instanceof ELitInt) {
                return new AnnotatedExpr<>(
            }
            return "multiplication";
        }

        public AnnotatedExpr<?> visit(Div p, EnvOptimizer env) {
            return "division";
        }

        public AnnotatedExpr<?> visit(Mod p, EnvOptimizer env) {
            return "modulo";
        }
    }

    public static class RelOpVisitor implements RelOp.Visitor<String, TypeCode[]> {
        private boolean bothTypes(TypeCode[] actual, TypeCode... expected) {
            TypeCode left = actual[0];
            TypeCode right = actual[1];
            return left == right && Arrays.asList(expected).contains(left);
        }

        public String visit(LTH p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CInt, TypeCode.CDouble)
                   ? null
                   : "lower than";
        }

        public String visit(LE p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CInt, TypeCode.CDouble)
                   ? null
                   : "lower or equal";
        }

        public String visit(GTH p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CInt, TypeCode.CDouble)
                   ? null
                   : "greater than";
        }

        public String visit(GE p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CInt, TypeCode.CDouble)
                   ? null
                   : "greater or equal";
        }

        public String visit(EQU p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CInt, TypeCode.CDouble, TypeCode.CBool)
                   ? null
                   : "equality";
        }

        public String visit(NE p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CInt, TypeCode.CDouble, TypeCode.CBool)
                   ? null
                   : "difference";
        }
    }

}
