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

import java.util.LinkedList;

class EnvTypecheck extends Env<TypeCode, FunType> {
    public TypeCode currentFunctionType = null;
}

public class TypeChecker {
    public static boolean canCoerce(TypeCode from, TypeCode to) {
        return from != null && from == to
               || from == TypeCode.CInt && to == TypeCode.CDouble;
    }

    public static TypeCode minType(TypeCode left, TypeCode right) {
        if (left == right) {
            return left;
        } else if (left == TypeCode.CDouble && right == TypeCode.CInt
                   || left == TypeCode.CInt && right == TypeCode.CDouble) {
            return TypeCode.CDouble;
        } else {
            return null;
        }
    }

    public Prog typecheck(Prog p) {
        EnvTypecheck env = new EnvTypecheck();
        p.accept(new ProgVisitorSignature(), env);
        return p.accept(new ProgVisitor(), env);
    }

    public static class ProgVisitorSignature implements Prog.Visitor<Void, EnvTypecheck> {
        public Void visit(Program p, EnvTypecheck env) {
            for (TopDef def : p.listtopdef_) {
                def.accept(new TopDefVisitorSignature(), env);
            }

            env.insertFun("printInt", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CInt, "i")
            ));
            env.insertFun("printDouble", new FunType(
                TypeCode.CVoid,
                new FunArg(TypeCode.CDouble, "d")
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
        public Prog visit(Program p, EnvTypecheck env) {
            ListTopDef topDef = new ListTopDef();

            for (TopDef def : p.listtopdef_) {
                topDef.add(def.accept(new TopDefVisitor(), env));
            }

            return new Program(topDef);
        }
    }

    public static class TopDefVisitor implements TopDef.Visitor<TopDef, EnvTypecheck> {
        public TopDef visit(FnDef f, EnvTypecheck env) {
            FunType func = env.lookupFun(f.ident_);

            env.enterScope();

            for (FunArg arg : func.args) {
                env.insertVar(arg.name, arg.type);
            }

            env.currentFunctionType = func.retType;

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

    public static class TopDefVisitorSignature implements TopDef.Visitor<Void, EnvTypecheck> {
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
        public Blk visit(Block p, EnvTypecheck env) {
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
        public Stmt visit(Empty s, EnvTypecheck env) {
            return new Empty();
        }

        public Stmt visit(BStmt s, EnvTypecheck env) {
            return new BStmt(s.blk_.accept(new BlkVisitor(), env));
        }

        public Stmt visit(Decl s, EnvTypecheck env) {
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

        public Stmt visit(Ass s, EnvTypecheck env) {
            TypeCode expectedType = env.lookupVar(s.ident_);
            if (expectedType == null) {
                throw new NoSuchVariableException(s.ident_);
            }

            ExpCustom exp = s.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(exp.type, expectedType)) {
                throw new InvalidAssignmentTypeException(
                    s.ident_,
                    expectedType.toString(),
                    exp.type.toString()
                );
            }

            return new Ass(s.ident_, exp.maybeCoertTo(expectedType));
        }

        public Stmt visit(Incr s, EnvTypecheck env) {
            TypeCode varType = env.lookupVar(s.ident_);

            if (varType == null) {
                throw new NoSuchVariableException(s.ident_);
            }

            if (!canCoerce(varType, TypeCode.CDouble)) {
                throw new InvalidOperationException(
                    "increment",
                    varType.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new Incr(s.ident_);
        }

        public Stmt visit(Decr s, EnvTypecheck env) {
            TypeCode varType = env.lookupVar(s.ident_);

            if (varType == null) {
                throw new NoSuchVariableException(s.ident_);
            }

            if (!canCoerce(varType, TypeCode.CDouble)) {
                throw new InvalidOperationException(
                    "decrement",
                    varType.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new Decr(s.ident_);
        }

        public Stmt visit(Ret s, EnvTypecheck env) {
            ExpCustom exp = s.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(exp.type, env.currentFunctionType)) {
                throw new InvalidReturnedTypeException(
                    env.currentFunctionType.toString(),
                    exp.type.toString()
                );
            }

            return new Ret(exp.maybeCoertTo(env.currentFunctionType));
        }

        public Stmt visit(VRet s, EnvTypecheck env) {
            if (env.currentFunctionType != TypeCode.CVoid) {
                throw new InvalidReturnedTypeException(
                    env.currentFunctionType.toString(),
                    TypeCode.CVoid.toString()
                );
            }

            return new VRet();
        }

        public Stmt visit(Cond s, EnvTypecheck env) {
            ExpCustom exp = s.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(exp.type, TypeCode.CBool)) {
                throw new InvalidConditionTypeException("if", exp.type.toString());
            }

            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();

            return new Cond(exp.maybeCoertTo(TypeCode.CBool), stmt);
        }

        public Stmt visit(CondElse s, EnvTypecheck env) {
            ExpCustom exp = s.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(exp.type, TypeCode.CBool)) {
                throw new InvalidConditionTypeException("if-else", exp.type.toString());
            }

            env.enterScope();
            Stmt stmt1 = s.stmt_1.accept(new StmtVisitor(), env);
            env.leaveScope();

            env.enterScope();
            Stmt stmt2 = s.stmt_2.accept(new StmtVisitor(), env);
            env.leaveScope();

            return new CondElse(exp.maybeCoertTo(TypeCode.CBool), stmt1, stmt2);
        }

        public Stmt visit(While s, EnvTypecheck env) {
            ExpCustom exp = s.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(exp.type, TypeCode.CBool)) {
                throw new InvalidConditionTypeException("while", exp.type.toString());
            }

            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();

            return new While(exp.maybeCoertTo(TypeCode.CBool), stmt);
        }

        public Stmt visit(SExp s, EnvTypecheck env) {
            return new SExp(s.expr_.accept(new ExprVisitor(), env));
        }
    }

    public static class ItemVisitor implements Item.Visitor<Item, Object[]> {
        public Item visit(NoInit p, Object[] args) {
            EnvTypecheck env = (EnvTypecheck) args[0];
            TypeCode varType = (TypeCode) args[1];
            env.insertVar(p.ident_, varType);
            return new NoInit(p.ident_);
        }

        public Item visit(Init s, Object[] args) {
            EnvTypecheck env = (EnvTypecheck) args[0];
            TypeCode varType = (TypeCode) args[1];
            env.insertVar(s.ident_, varType);
            ExpCustom exp = s.expr_.accept(new ExprVisitor(), env);

            if (!canCoerce(exp.type, varType)) {
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

    public static class ExprVisitor implements Expr.Visitor<ExpCustom, EnvTypecheck> {
        public ExpCustom visit(EVar e, EnvTypecheck env) {
            TypeCode varType = env.lookupVar(e.ident_);
            if (varType == null) {
                throw new NoSuchVariableException(e.ident_);
            }

            return new ExpCustom(varType, e);
        }

        public ExpCustom visit(ELitInt e, EnvTypecheck env) {
            return new ExpCustom(TypeCode.CInt, e);
        }

        public ExpCustom visit(ELitDoub e, EnvTypecheck env) {
            return new ExpCustom(TypeCode.CDouble, e);
        }

        public ExpCustom visit(ELitTrue e, EnvTypecheck env) {
            return new ExpCustom(TypeCode.CBool, e);
        }

        public ExpCustom visit(ELitFalse e, EnvTypecheck env) {
            return new ExpCustom(TypeCode.CBool, e);
        }

        public ExpCustom visit(EApp e, EnvTypecheck env) {
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
                ExpCustom exp = e.listexpr_.get(i).accept(new ExprVisitor(), env);
                if (!canCoerce(exp.type, expected.type)) {
                    throw new InvalidAssignmentTypeException(
                        expected.name,
                        expected.type.toString(),
                        exp.type.toString()
                    );
                }
                exps.add(exp.maybeCoertTo(expected.type));
            }

            return new ExpCustom(funcType.retType, new EApp(e.ident_, exps));
        }

        public ExpCustom visit(EString e, EnvTypecheck env) {
            throw new UnsupportedOperationException("visit(javalette.Absyn.EString)");
        }

        public ExpCustom visit(Neg e, EnvTypecheck env) {
            ExpCustom expr = e.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(expr.type, TypeCode.CDouble)) {
                throw new InvalidOperationException(
                    "negation",
                    expr.type.toString(),
                    TypeCode.CInt.toString(),
                    TypeCode.CDouble.toString()
                );
            }

            return new ExpCustom(expr.type, new Neg(expr));
        }

        public ExpCustom visit(Not e, EnvTypecheck env) {
            ExpCustom expr = e.expr_.accept(new ExprVisitor(), env);
            if (!canCoerce(expr.type, TypeCode.CBool)) {
                throw new InvalidOperationException(
                    "not",
                    expr.type.toString(),
                    TypeCode.CBool.toString()
                );
            }

            return new ExpCustom(
                TypeCode.CBool,
                new Not(expr.maybeCoertTo(TypeCode.CBool))
            );
        }

        public ExpCustom visit(EMul e, EnvTypecheck env) {
            ExpCustom left = e.expr_1.accept(new ExprVisitor(), env);
            ExpCustom right = e.expr_2.accept(new ExprVisitor(), env);
            TypeCode min = minType(left.type, right.type);

            if (!canCoerce(min, TypeCode.CDouble)) {
                throw new InvalidOperationException(
                    e.mulop_.accept(new MulOpVisitor(), null),
                    left.type.toString(),
                    right.type.toString()
                );
            }

            return new ExpCustom(
                min,
                new EMul(
                    left.maybeCoertTo(min),
                    e.mulop_,
                    right.maybeCoertTo(min)
                )
            );
        }

        public ExpCustom visit(EAdd e, EnvTypecheck env) {
            ExpCustom left = e.expr_1.accept(new ExprVisitor(), env);
            ExpCustom right = e.expr_2.accept(new ExprVisitor(), env);
            TypeCode min = minType(left.type, right.type);

            if (!canCoerce(min, TypeCode.CDouble)) {
                throw new InvalidOperationException(
                    e.addop_.accept(new AddOpVisitor(), null),
                    left.type.toString(),
                    right.type.toString()
                );
            }

            return new ExpCustom(
                min,
                new EAdd(
                    left.maybeCoertTo(min),
                    e.addop_,
                    right.maybeCoertTo(min)
                )
            );
        }

        public ExpCustom visit(ERel e, EnvTypecheck env) {
            ExpCustom left = e.expr_1.accept(new ExprVisitor(), env);
            ExpCustom right = e.expr_2.accept(new ExprVisitor(), env);
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

            TypeCode min = minType(left.type, right.type);
            return new ExpCustom(
                TypeCode.CBool,
                new ERel(
                    left.maybeCoertTo(min),
                    e.relop_,
                    right.maybeCoertTo(min)
                )
            );
        }

        public ExpCustom visit(EAnd e, EnvTypecheck env) {
            ExpCustom left = e.expr_1.accept(new ExprVisitor(), env);
            ExpCustom right = e.expr_2.accept(new ExprVisitor(), env);

            if (!canCoerce(left.type, TypeCode.CBool) || !canCoerce(right.type, TypeCode.CBool)) {
                throw new InvalidOperationException(
                    "conjunction",
                    left.type.toString(),
                    right.type.toString()
                );
            }

            return new ExpCustom(
                TypeCode.CBool,
                new EAnd(
                    left.maybeCoertTo(TypeCode.CBool),
                    right.maybeCoertTo(TypeCode.CBool)
                )
            );
        }

        public ExpCustom visit(EOr e, EnvTypecheck env) {
            ExpCustom left = e.expr_1.accept(new ExprVisitor(), env);
            ExpCustom right = e.expr_2.accept(new ExprVisitor(), env);

            if (!canCoerce(left.type, TypeCode.CBool) || !canCoerce(right.type, TypeCode.CBool)) {
                throw new InvalidOperationException(
                    "disjunction",
                    left.type.toString(),
                    right.type.toString()
                );
            }

            return new ExpCustom(
                TypeCode.CBool,
                new EOr(
                    left.maybeCoertTo(TypeCode.CBool),
                    right.maybeCoertTo(TypeCode.CBool)
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
        private boolean bothTypes(TypeCode[] types, TypeCode type) {
            TypeCode left = types[0];
            TypeCode right = types[1];
            return canCoerce(minType(left, right), type);
        }

        public String visit(LTH p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CDouble) ? null : "lower than";
        }

        public String visit(LE p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CDouble) ? null : "lower or equal";
        }

        public String visit(GTH p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CDouble) ? null : "greater than";
        }

        public String visit(GE p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CDouble) ? null : "greater or equal";
        }

        public String visit(EQU p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CDouble) || bothTypes(types, TypeCode.CBool)
                   ? null
                   : "equality";
        }

        public String visit(NE p, TypeCode[] types) {
            return bothTypes(types, TypeCode.CDouble) || bothTypes(types, TypeCode.CBool)
                   ? null
                   : "difference";
        }
    }
}
