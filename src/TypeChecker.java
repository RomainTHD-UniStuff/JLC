import javalette.Absyn.AddOp;
import javalette.Absyn.Arg;
import javalette.Absyn.Argument;
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
import java.util.LinkedList;

class EnvTypecheck extends Env<TypeCode, FunType> {
    public TypeCode currentFunctionType = null;

    private boolean _doesReturn;

    public boolean doesReturn() {
        return this._doesReturn;
    }

    public void setReturn(boolean doesReturn) {
        this._doesReturn = doesReturn;
    }
}

public class TypeChecker {
    public Prog typecheck(Prog p) {
        EnvTypecheck env = new EnvTypecheck();
        p.accept(new ProgSignatureVisitor(), env);
        return p.accept(new ProgVisitor(), env);
    }

    public static class ProgSignatureVisitor implements Prog.Visitor<Void, EnvTypecheck> {
        public Void visit(Program p, EnvTypecheck env) {
            for (TopDef def : p.listtopdef_) {
                def.accept(new TopDefSignatureVisitor(), env);
            }

            env.insertFun("printInt", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CInt, "i")
            ));
            env.insertFun("printDouble", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CDouble, "d")
            ));
            env.insertFun("printString", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CString, "s")
            ));
            env.insertFun("readInt", new FunType(TypeCode.CInt));
            env.insertFun("readDouble", new FunType(TypeCode.CDouble));

            FunType mainFunc = env.lookupFun("main");
            if (mainFunc == null) {
                // FIXME: Should it be there or in the compiler directly?
                throw new NoSuchFunctionException("main");
            }

            if (mainFunc.retType != TypeCode.CInt) {
                throw new InvalidReturnedTypeException(
                    "main",
                    TypeCode.CInt.toString(),
                    mainFunc.retType.toString()
                );
            }

            if (!mainFunc.args.isEmpty()) {
                throw new InvalidArgumentCountException(
                    "main",
                    0,
                    mainFunc.args.size()
                );
            }

            return null;
        }
    }

    public static class ProgVisitor implements Prog.Visitor<Prog, EnvTypecheck> {
        public Program visit(Program p, EnvTypecheck env) {
            ListTopDef topDef = new ListTopDef();

            for (TopDef def : p.listtopdef_) {
                topDef.add(def.accept(new TopDefVisitor(), env));
            }

            return new Program(topDef);
        }
    }

    public static class TopDefVisitor implements TopDef.Visitor<TopDef, EnvTypecheck> {
        public FnDef visit(FnDef f, EnvTypecheck env) {
            FunType func = env.lookupFun(f.ident_);

            env.setReturn(false);
            env.enterScope();

            for (FunArg arg : func.args) {
                env.insertVar(arg.name, arg.type);
            }

            env.currentFunctionType = func.retType;

            Blk nBlock = f.blk_.accept(new BlkVisitor(), env);

            env.leaveScope();
            if (func.retType != TypeCode.CVoid && !env.doesReturn()) {
                throw new NoReturnException(f.ident_);
            }

            return new FnDef(
                f.type_,
                f.ident_,
                f.listarg_,
                nBlock
            );
        }
    }

    public static class TopDefSignatureVisitor implements TopDef.Visitor<Void, EnvTypecheck> {
        public Void visit(FnDef p, EnvTypecheck env) {
            LinkedList<FunArg> argsType = new LinkedList<>();
            for (Arg arg : p.listarg_) {
                argsType.add(arg.accept(new ArgVisitor(), null));
            }

            TypeCode retType = p.type_.accept(new TypeVisitor(), null);
            env.insertFun(p.ident_, new FunType(retType, argsType));

            return null;
        }
    }

    public static class ArgVisitor implements Arg.Visitor<FunArg, Void> {
        public FunArg visit(Argument a, Void ignored) {
            TypeCode type = a.type_.accept(new TypeVisitor(), null);

            if (type == TypeCode.CVoid) {
                throw new InvalidDeclaredTypeException(type.toString(), a.ident_);
            }

            return new FunArg(type, a.ident_);
        }
    }

    public static class BlkVisitor implements Blk.Visitor<Blk, EnvTypecheck> {
        public Block visit(Block p, EnvTypecheck env) {
            ListStmt statements = new ListStmt();

            env.enterScope();

            for (Stmt s : p.liststmt_) {
                statements.add(s.accept(new StmtVisitor(), env));
            }

            env.leaveScope();

            return new Block(statements);
        }
    }

    public static class StmtVisitor implements Stmt.Visitor<Stmt, EnvTypecheck> {
        public Empty visit(Empty s, EnvTypecheck env) {
            return new Empty();
        }

        public BStmt visit(BStmt s, EnvTypecheck env) {
            return new BStmt(s.blk_.accept(new BlkVisitor(), env));
        }

        public Decl visit(Decl s, EnvTypecheck env) {
            TypeCode type = s.type_.accept(new TypeVisitor(), null);
            if (type == TypeCode.CVoid) {
                throw new InvalidDeclaredTypeException(
                    type.toString()
                );
            }

            ListItem items = new ListItem();

            for (Item item : s.listitem_) {
                items.add(item.accept(new ItemVisitor(), new Object[]{env, type}));
            }

            return new Decl(s.type_, items);
        }

        public Ass visit(Ass s, EnvTypecheck env) {
            TypeCode expectedType = env.lookupVar(s.ident_);
            if (expectedType == null) {
                throw new NoSuchVariableException(s.ident_);
            }

            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != expectedType) {
                throw new InvalidAssignmentTypeException(
                    s.ident_,
                    expectedType.toString(),
                    exp.type.toString()
                );
            }

            return new Ass(s.ident_, exp);
        }

        public Incr visit(Incr s, EnvTypecheck env) {
            TypeCode varType = env.lookupVar(s.ident_);

            if (varType == null) {
                throw new NoSuchVariableException(s.ident_);
            }

            if (varType != TypeCode.CInt) {
                throw new InvalidOperationException(
                    "increment",
                    varType.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new Incr(s.ident_);
        }

        public Decr visit(Decr s, EnvTypecheck env) {
            TypeCode varType = env.lookupVar(s.ident_);

            if (varType == null) {
                throw new NoSuchVariableException(s.ident_);
            }

            if (varType != TypeCode.CInt) {
                throw new InvalidOperationException(
                    "decrement",
                    varType.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new Decr(s.ident_);
        }

        public Ret visit(Ret s, EnvTypecheck env) {
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != env.currentFunctionType) {
                throw new InvalidReturnedTypeException(
                    env.currentFunctionType.toString(),
                    exp.type.toString()
                );
            }

            env.setReturn(true);
            return new Ret(exp);
        }

        public VRet visit(VRet s, EnvTypecheck env) {
            if (env.currentFunctionType != TypeCode.CVoid) {
                throw new InvalidReturnedTypeException(
                    env.currentFunctionType.toString(),
                    TypeCode.CVoid.toString()
                );
            }

            env.setReturn(true);
            return new VRet();
        }

        public Cond visit(Cond s, EnvTypecheck env) {
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != TypeCode.CBool) {
                throw new InvalidConditionTypeException("if", exp.type.toString());
            }

            boolean doesReturn = env.doesReturn();

            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();

            env.setReturn(doesReturn);

            return new Cond(exp, stmt);
        }

        public CondElse visit(CondElse s, EnvTypecheck env) {
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != TypeCode.CBool) {
                throw new InvalidConditionTypeException("if-else", exp.type.toString());
            }

            boolean doesReturn = env.doesReturn();

            env.enterScope();
            Stmt stmt1 = s.stmt_1.accept(new StmtVisitor(), env);
            env.leaveScope();

            boolean doesReturnIf = env.doesReturn();
            env.setReturn(doesReturn);

            env.enterScope();
            Stmt stmt2 = s.stmt_2.accept(new StmtVisitor(), env);
            env.leaveScope();

            boolean doesReturnElse = env.doesReturn();

            env.setReturn(doesReturn || (doesReturnIf && doesReturnElse));

            return new CondElse(exp, stmt1, stmt2);
        }

        public While visit(While s, EnvTypecheck env) {
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != TypeCode.CBool) {
                throw new InvalidConditionTypeException("while", exp.type.toString());
            }

            boolean doesReturn = env.doesReturn();

            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();

            env.setReturn(doesReturn);

            return new While(exp, stmt);
        }

        public SExp visit(SExp s, EnvTypecheck env) {
            AnnotatedExpr<?> expr = s.expr_.accept(new ExprVisitor(), env);

            if (expr.type != TypeCode.CVoid) {
                throw new InvalidExpressionTypeException(
                    expr.type.toString(),
                    TypeCode.CVoid.toString()
                );
            }

            return new SExp(expr);
        }
    }

    public static class ItemVisitor implements Item.Visitor<Item, Object[]> {
        public NoInit visit(NoInit p, Object[] args) {
            EnvTypecheck env = (EnvTypecheck) args[0];
            TypeCode varType = (TypeCode) args[1];
            env.insertVar(p.ident_, varType);
            return new NoInit(p.ident_);
        }

        public Init visit(Init s, Object[] args) {
            EnvTypecheck env = (EnvTypecheck) args[0];
            TypeCode varType = (TypeCode) args[1];
            env.insertVar(s.ident_, varType);
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);

            if (exp.type != varType) {
                throw new InvalidAssignmentTypeException(
                    s.ident_,
                    varType.toString(),
                    exp.type.toString()
                );
            }

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

    public static class ExprVisitor implements Expr.Visitor<AnnotatedExpr<?>, EnvTypecheck> {
        public AnnotatedExpr<EVar> visit(EVar e, EnvTypecheck env) {
            TypeCode varType = env.lookupVar(e.ident_);
            if (varType == null) {
                throw new NoSuchVariableException(e.ident_);
            }

            return new AnnotatedExpr<>(varType, e);
        }

        public AnnotatedExpr<ELitInt> visit(ELitInt e, EnvTypecheck env) {
            return new AnnotatedExpr<>(TypeCode.CInt, e);
        }

        public AnnotatedExpr<ELitDoub> visit(ELitDoub e, EnvTypecheck env) {
            return new AnnotatedExpr<>(TypeCode.CDouble, e);
        }

        public AnnotatedExpr<ELitTrue> visit(ELitTrue e, EnvTypecheck env) {
            return new AnnotatedExpr<>(TypeCode.CBool, e);
        }

        public AnnotatedExpr<ELitFalse> visit(ELitFalse e, EnvTypecheck env) {
            return new AnnotatedExpr<>(TypeCode.CBool, e);
        }

        public AnnotatedExpr<EApp> visit(EApp e, EnvTypecheck env) {
            FunType funcType = env.lookupFun(e.ident_);
            if (funcType == null) {
                throw new NoSuchFunctionException(e.ident_);
            }

            if (e.listexpr_.size() != funcType.args.size()) {
                throw new InvalidArgumentCountException(
                    e.ident_,
                    funcType.args.size(),
                    e.listexpr_.size()
                );
            }

            ListExpr exps = new ListExpr();
            for (int i = 0; i < funcType.args.size(); ++i) {
                FunArg expected = funcType.args.get(i);
                AnnotatedExpr<?> exp = e.listexpr_.get(i).accept(new ExprVisitor(), env);
                if (exp.type != expected.type) {
                    throw new InvalidAssignmentTypeException(
                        expected.name,
                        expected.type.toString(),
                        exp.type.toString(),
                        true
                    );
                }
                exps.add(exp);
            }

            return new AnnotatedExpr<>(funcType.retType, new EApp(e.ident_, exps));
        }

        public AnnotatedExpr<EString> visit(EString e, EnvTypecheck env) {
            return new AnnotatedExpr<>(TypeCode.CString, e);
        }

        public AnnotatedExpr<Neg> visit(Neg e, EnvTypecheck env) {
            AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
            if (expr.type != TypeCode.CInt && expr.type != TypeCode.CDouble) {
                throw new InvalidOperationException(
                    "negation",
                    expr.type.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new AnnotatedExpr<>(expr.type, new Neg(expr));
        }

        public AnnotatedExpr<Not> visit(Not e, EnvTypecheck env) {
            AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
            if (expr.type != TypeCode.CBool) {
                throw new InvalidOperationException(
                    "not",
                    expr.type.toString(),
                    TypeCode.CBool.toString()
                );
            }

            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new Not(expr)
            );
        }

        public AnnotatedExpr<EMul> visit(EMul e, EnvTypecheck env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

            if (left.type != right.type) {
                throw new InvalidOperationException(
                    e.mulop_.accept(new MulOpVisitor(), null),
                    left.type.toString(),
                    right.type.toString()
                );
            }

            if (e.mulop_ instanceof Mod) {
                if (left.type != TypeCode.CInt) {
                    throw new InvalidOperationException(
                        e.mulop_.accept(new MulOpVisitor(), null),
                        left.type.toString(),
                        TypeCode.CInt.toString()
                    );
                }
            }

            if (left.type != TypeCode.CInt && left.type != TypeCode.CDouble) {
                throw new InvalidOperationException(
                    e.mulop_.accept(new MulOpVisitor(), null),
                    left.type.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new AnnotatedExpr<>(
                left.type,
                new EMul(
                    left,
                    e.mulop_,
                    right
                )
            );
        }

        public AnnotatedExpr<EAdd> visit(EAdd e, EnvTypecheck env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

            if (left.type != right.type) {
                throw new InvalidOperationException(
                    e.addop_.accept(new AddOpVisitor(), null),
                    left.type.toString(),
                    right.type.toString()
                );
            }

            if (left.type != TypeCode.CInt && left.type != TypeCode.CDouble) {
                throw new InvalidOperationException(
                    e.addop_.accept(new AddOpVisitor(), null),
                    left.type.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new AnnotatedExpr<>(
                left.type,
                new EAdd(
                    left,
                    e.addop_,
                    right
                )
            );
        }

        public AnnotatedExpr<ERel> visit(ERel e, EnvTypecheck env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);
            String opName = e.relop_.accept(
                new RelOpVisitor(),
                new TypeCode[]{left.type, right.type}
            );

            if (opName != null) {
                throw new InvalidOperationException(
                    opName,
                    left.type.toString(),
                    right.type.toString()
                );
            }

            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new ERel(
                    left,
                    e.relop_,
                    right
                )
            );
        }

        public AnnotatedExpr<EAnd> visit(EAnd e, EnvTypecheck env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

            if (left.type != TypeCode.CBool || right.type != TypeCode.CBool) {
                throw new InvalidOperationException(
                    "conjunction",
                    left.type.toString(),
                    right.type.toString()
                );
            }

            return new AnnotatedExpr<>(
                TypeCode.CBool,
                new EAnd(
                    left,
                    right
                )
            );
        }

        public AnnotatedExpr<EOr> visit(EOr e, EnvTypecheck env) {
            AnnotatedExpr<?> left = e.expr_1.accept(new ExprVisitor(), env);
            AnnotatedExpr<?> right = e.expr_2.accept(new ExprVisitor(), env);

            if (left.type != TypeCode.CBool || right.type != TypeCode.CBool) {
                throw new InvalidOperationException(
                    "disjunction",
                    left.type.toString(),
                    right.type.toString()
                );
            }

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

    public static class MulOpVisitor implements MulOp.Visitor<String, Void> {
        public String visit(Times p, Void ignored) {
            return "multiplication";
        }

        public String visit(Div p, Void ignored) {
            return "division";
        }

        public String visit(Mod p, Void ignored) {
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
