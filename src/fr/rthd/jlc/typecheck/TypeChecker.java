package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.Choice;
import fr.rthd.jlc.internal.NotImplementedException;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.InvalidArgumentCountException;
import fr.rthd.jlc.typecheck.exception.InvalidAssignmentTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidConditionTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidDeclaredTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidExpressionTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidNewTypeException;
import fr.rthd.jlc.typecheck.exception.InvalidOperationException;
import fr.rthd.jlc.typecheck.exception.InvalidReturnedTypeException;
import fr.rthd.jlc.typecheck.exception.NoReturnException;
import fr.rthd.jlc.typecheck.exception.NoSuchFunctionException;
import fr.rthd.jlc.typecheck.exception.NoSuchVariableException;
import javalette.Absyn.AddOp;
import javalette.Absyn.Arg;
import javalette.Absyn.Argument;
import javalette.Absyn.ArrayCon;
import javalette.Absyn.Ass;
import javalette.Absyn.AttrMember;
import javalette.Absyn.BStmt;
import javalette.Absyn.Blk;
import javalette.Absyn.Block;
import javalette.Absyn.ClassDef;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Constructor;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.Div;
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
import javalette.Absyn.EOr;
import javalette.Absyn.EQU;
import javalette.Absyn.ERel;
import javalette.Absyn.ESelf;
import javalette.Absyn.EString;
import javalette.Absyn.EVar;
import javalette.Absyn.Empty;
import javalette.Absyn.Expr;
import javalette.Absyn.Extend;
import javalette.Absyn.FnDef;
import javalette.Absyn.FnMember;
import javalette.Absyn.For;
import javalette.Absyn.FuncDef;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.Incr;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.ListExpr;
import javalette.Absyn.ListItem;
import javalette.Absyn.ListMember;
import javalette.Absyn.ListStmt;
import javalette.Absyn.ListTopDef;
import javalette.Absyn.Member;
import javalette.Absyn.Minus;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.NE;
import javalette.Absyn.Neg;
import javalette.Absyn.NoExtend;
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
import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import javalette.Absyn.TypeCon;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Type checker
 * @author RomainTHD
 */
public class TypeChecker implements Visitor {
    @Override
    public Prog accept(Prog p, Env<?, FunType, ClassType> parentEnv) {
        EnvTypecheck env = new EnvTypecheck(parentEnv);
        p.accept(new ProgSignatureVisitor(), env);
        return p.accept(new ProgVisitor(), env);
    }

    private static class ProgSignatureVisitor implements Prog.Visitor<Void, EnvTypecheck> {
        public Void visit(Program p, EnvTypecheck env) {
            for (TopDef def : p.listtopdef_) {
                def.accept(new TopDefSignatureVisitor(), env);
            }

            env.insertFun(new FunType(
                TypeCode.CVoid,
                "printInt",
                new FunArg(TypeCode.CInt, "i")
            ).setExternal().setPure(Choice.FALSE));

            env.insertFun(new FunType(
                TypeCode.CVoid,
                "printDouble",
                new FunArg(TypeCode.CDouble, "d")
            ).setExternal().setPure(Choice.FALSE));

            env.insertFun(new FunType(
                TypeCode.CVoid,
                "printString",
                new FunArg(TypeCode.CString, "s")
            ).setExternal().setPure(Choice.FALSE));

            env.insertFun(new FunType(
                TypeCode.CInt,
                "readInt"
            ).setExternal().setPure(Choice.FALSE));

            env.insertFun(new FunType(
                TypeCode.CDouble,
                "readDouble"
            ).setExternal().setPure(Choice.FALSE));

            FunType mainFunc = env.lookupFun("main");
            if (mainFunc == null) {
                throw new NoSuchFunctionException("main");
            } else {
                mainFunc.setAsMain();
            }

            if (mainFunc.retType != TypeCode.CInt) {
                throw new InvalidReturnedTypeException(
                    "main",
                    TypeCode.CInt,
                    mainFunc.retType
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

    private static class TopDefSignatureVisitor implements TopDef.Visitor<Void, EnvTypecheck> {
        public Void visit(TopFnDef p, EnvTypecheck env) {
            return p.funcdef_.accept(new FuncDefSignatureVisitor(), env);
        }

        public Void visit(TopClsDef p, EnvTypecheck env) {
            return p.classdef_.accept(new ClassDefSignatureVisitor(), env);
        }
    }

    private static class FuncDefSignatureVisitor implements FuncDef.Visitor<Void, EnvTypecheck> {
        public Void visit(FnDef p, EnvTypecheck env) {
            List<FunArg> argsType = new LinkedList<>();
            for (Arg arg : p.listarg_) {
                argsType.add(arg.accept(new ArgVisitor(), null));
            }

            TypeCode retType = p.type_.accept(new TypeVisitor(), null);
            env.insertFun(new FunType(retType, p.ident_, argsType));

            return null;
        }
    }

    private static class ClassDefSignatureVisitor implements ClassDef.Visitor<Void, EnvTypecheck> {
        private Void visit(
            String name,
            String superclass,
            ListMember members,
            EnvTypecheck env
        ) {
            List<FunType> methods = new ArrayList<>();
            List<Attribute> attributes = new ArrayList<>();

            // FIXME: This will not work if a function header uses a class still
            //  not reached. For example, `void f(A a) {} ... class A {}`
            //  will throw an error, since A is still unknown while f is being
            //  defined.

            for (Member m : members) {
                if (m instanceof FnMember) {
                    FnDef f = (FnDef) ((FnMember) m).funcdef_;
                    List<FunArg> args = new LinkedList<>();
                    for (Arg arg : f.listarg_) {
                        args.add(arg.accept(new ArgVisitor(), null));
                    }
                    methods.add(new FunType(
                        f.type_.accept(new TypeVisitor(), null),
                        f.ident_,
                        args
                    ));
                } else if (m instanceof AttrMember) {
                    AttrMember a = (AttrMember) m;
                    attributes.add(new Attribute(
                        a.type_.accept(new TypeVisitor(), null),
                        a.ident_
                    ));
                } else {
                    throw new IllegalArgumentException(
                        "Unknown member type: " + m.getClass().getName()
                    );
                }
            }

            ClassType c;
            if (superclass == null) {
                c = new ClassType(name, methods, attributes);
            } else {
                c = new ClassType(name, superclass, methods, attributes);
            }

            env.insertClass(c);
            return null;
        }

        public Void visit(NoExtend p, EnvTypecheck env) {
            return visit(
                p.ident_,
                null,
                p.listmember_,
                env
            );
        }

        public Void visit(Extend p, EnvTypecheck env) {
            return visit(
                p.ident_1,
                p.ident_2,
                p.listmember_,
                env
            );
        }
    }

    private static class ProgVisitor implements Prog.Visitor<Prog, EnvTypecheck> {
        public Program visit(Program p, EnvTypecheck env) {
            ListTopDef topDef = new ListTopDef();

            for (TopDef def : p.listtopdef_) {
                topDef.add(def.accept(new TopDefVisitor(), env));
            }

            return new Program(topDef);
        }
    }

    private static class TopDefVisitor implements TopDef.Visitor<TopDef, EnvTypecheck> {
        public TopFnDef visit(TopFnDef p, EnvTypecheck env) {
            return new TopFnDef(
                p.funcdef_.accept(new FuncDefVisitor(), env)
            );
        }

        public TopClsDef visit(TopClsDef p, EnvTypecheck env) {
            return new TopClsDef(
                p.classdef_.accept(new ClassDefVisitor(), env)
            );
        }
    }

    private static class ClassDefVisitor implements ClassDef.Visitor<ClassDef, EnvTypecheck> {
        public NoExtend visit(NoExtend p, EnvTypecheck env) {
            ListMember members = new ListMember();
            for (Member m : p.listmember_) {
                members.add(m.accept(new MemberVisitor(), env));
            }
            return new NoExtend(
                p.ident_,
                members
            );
        }

        public Extend visit(Extend p, EnvTypecheck env) {
            ListMember members = new ListMember();
            for (Member m : p.listmember_) {
                members.add(m.accept(new MemberVisitor(), env));
            }
            return new Extend(
                p.ident_1,
                p.ident_2,
                members
            );
        }
    }

    private static class MemberVisitor implements Member.Visitor<Member, EnvTypecheck> {
        public FnMember visit(FnMember p, EnvTypecheck env) {
            return new FnMember(
                p.funcdef_.accept(new FuncDefVisitor(), env)
            );
        }

        public AttrMember visit(AttrMember p, EnvTypecheck env) {
            return new AttrMember(
                p.type_,
                p.ident_
            );
        }
    }

    private static class FuncDefVisitor implements FuncDef.Visitor<FnDef, EnvTypecheck> {
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

    private static class ArgVisitor implements Arg.Visitor<FunArg, Void> {
        public FunArg visit(Argument a, Void ignored) {
            TypeCode type = a.type_.accept(new TypeVisitor(), null);

            if (type == TypeCode.CVoid) {
                throw new InvalidDeclaredTypeException(type, a.ident_);
            }

            return new FunArg(type, a.ident_);
        }
    }

    private static class BlkVisitor implements Blk.Visitor<Blk, EnvTypecheck> {
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

    private static class StmtVisitor implements Stmt.Visitor<Stmt, EnvTypecheck> {
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
                    type
                );
            }

            ListItem items = new ListItem();

            for (Item item : s.listitem_) {
                items.add(item.accept(new ItemVisitor(type), env));
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
                    expectedType,
                    exp.type
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
                    varType,
                    TypeCode.CInt
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
                    varType,
                    TypeCode.CInt
                );
            }

            return new Decr(s.ident_);
        }

        public Ret visit(Ret s, EnvTypecheck env) {
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != env.currentFunctionType) {
                throw new InvalidReturnedTypeException(
                    env.currentFunctionType,
                    exp.type
                );
            }

            env.setReturn(true);
            return new Ret(exp);
        }

        public VRet visit(VRet s, EnvTypecheck env) {
            if (env.currentFunctionType != TypeCode.CVoid) {
                throw new InvalidReturnedTypeException(
                    env.currentFunctionType,
                    TypeCode.CVoid
                );
            }

            env.setReturn(true);
            return new VRet();
        }

        public Cond visit(Cond s, EnvTypecheck env) {
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
            if (exp.type != TypeCode.CBool) {
                throw new InvalidConditionTypeException("if", exp.type);
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
                throw new InvalidConditionTypeException("if-else", exp.type);
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
                throw new InvalidConditionTypeException("while", exp.type);
            }

            boolean doesReturn = env.doesReturn();

            env.enterScope();
            Stmt stmt = s.stmt_.accept(new StmtVisitor(), env);
            env.leaveScope();

            env.setReturn(doesReturn);

            return new While(exp, stmt);
        }

        public For visit(For p, EnvTypecheck env) {
            throw new NotImplementedException();
        }

        public SExp visit(SExp s, EnvTypecheck env) {
            AnnotatedExpr<?> expr = s.expr_.accept(new ExprVisitor(), env);

            if (expr.type != TypeCode.CVoid) {
                throw new InvalidExpressionTypeException(
                    expr.type,
                    TypeCode.CVoid
                );
            }

            return new SExp(expr);
        }
    }

    private static class ItemVisitor implements Item.Visitor<Item, EnvTypecheck> {
        private final TypeCode varType;

        public ItemVisitor(TypeCode varType) {
            this.varType = varType;
        }

        public NoInit visit(NoInit p, EnvTypecheck env) {
            env.insertVar(p.ident_, varType);
            return new NoInit(p.ident_);
        }

        public Init visit(Init s, EnvTypecheck env) {
            env.insertVar(s.ident_, varType);
            AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);

            if (exp.type != varType) {
                throw new InvalidAssignmentTypeException(
                    s.ident_,
                    varType,
                    exp.type
                );
            }

            return new Init(s.ident_, exp);
        }
    }

    private static class ConstructorVisitor implements Constructor.Visitor<TypeCode, EnvTypecheck> {
        public TypeCode visit(TypeCon p, EnvTypecheck env) {
            TypeCode t = p.type_.accept(new TypeVisitor(), null);
            if (env.lookupClass(t.getRealName()) == null) {
                throw new InvalidDeclaredTypeException(t);
            }

            return t;
        }

        public TypeCode visit(ArrayCon p, EnvTypecheck env) {
            throw new NotImplementedException();
        }
    }

    private static class ExprVisitor implements Expr.Visitor<AnnotatedExpr<?>, EnvTypecheck> {
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

        public AnnotatedExpr<ESelf> visit(ESelf p, EnvTypecheck env) {
            throw new NotImplementedException();
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
                AnnotatedExpr<?> exp = e
                    .listexpr_
                    .get(i)
                    .accept(
                        new ExprVisitor(),
                        env
                    );
                if (exp.type != expected.type) {
                    throw new InvalidAssignmentTypeException(
                        expected.name,
                        expected.type,
                        exp.type,
                        true
                    );
                }
                exps.add(exp);
            }

            return new AnnotatedExpr<>(
                funcType.retType,
                new EApp(e.ident_, exps)
            );
        }

        public AnnotatedExpr<EString> visit(EString e, EnvTypecheck env) {
            return new AnnotatedExpr<>(TypeCode.CString, e);
        }

        public AnnotatedExpr<EDot> visit(EDot p, EnvTypecheck env) {
            throw new NotImplementedException();
        }

        public AnnotatedExpr<EIndex> visit(EIndex p, EnvTypecheck env) {
            throw new NotImplementedException();
        }

        public AnnotatedExpr<ENew> visit(ENew p, EnvTypecheck env) {
            TypeCode t = p.constructor_.accept(new ConstructorVisitor(), env);
            if (t.isPrimitive()) {
                throw new InvalidNewTypeException(t);
            }

            return new AnnotatedExpr<>(
                t,
                new ENew(p.constructor_)
            );
        }

        public AnnotatedExpr<Neg> visit(Neg e, EnvTypecheck env) {
            AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
            if (expr.type != TypeCode.CInt && expr.type != TypeCode.CDouble) {
                throw new InvalidOperationException(
                    "negation",
                    expr.type,
                    TypeCode.CInt,
                    TypeCode.CDouble
                );
            }

            return new AnnotatedExpr<>(expr.type, new Neg(expr));
        }

        public AnnotatedExpr<Not> visit(Not e, EnvTypecheck env) {
            AnnotatedExpr<?> expr = e.expr_.accept(new ExprVisitor(), env);
            if (expr.type != TypeCode.CBool) {
                throw new InvalidOperationException(
                    "not",
                    expr.type,
                    TypeCode.CBool
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
                    left.type,
                    right.type
                );
            }

            if (e.mulop_ instanceof Mod) {
                if (left.type != TypeCode.CInt) {
                    throw new InvalidOperationException(
                        e.mulop_.accept(new MulOpVisitor(), null),
                        left.type,
                        TypeCode.CInt
                    );
                }
            }

            if (left.type != TypeCode.CInt && left.type != TypeCode.CDouble) {
                throw new InvalidOperationException(
                    e.mulop_.accept(new MulOpVisitor(), null),
                    left.type,
                    TypeCode.CInt,
                    TypeCode.CDouble
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
                    left.type,
                    right.type
                );
            }

            if (left.type != TypeCode.CInt && left.type != TypeCode.CDouble) {
                throw new InvalidOperationException(
                    e.addop_.accept(new AddOpVisitor(), null),
                    left.type,
                    TypeCode.CInt,
                    TypeCode.CDouble
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
                    left.type,
                    right.type
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
                    left.type,
                    right.type
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
                    left.type,
                    right.type
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

    private static class AddOpVisitor implements AddOp.Visitor<String, Void> {
        public String visit(Plus p, Void ignored) {
            return "addition";
        }

        public String visit(Minus p, Void ignored) {
            return "subtraction";
        }
    }

    private static class MulOpVisitor implements MulOp.Visitor<String, Void> {
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

    private static class RelOpVisitor implements RelOp.Visitor<String, TypeCode[]> {
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
            return bothTypes(
                types,
                TypeCode.CInt,
                TypeCode.CDouble,
                TypeCode.CBool
            )
                   ? null
                   : "equality";
        }

        public String visit(NE p, TypeCode[] types) {
            return bothTypes(
                types,
                TypeCode.CInt,
                TypeCode.CDouble,
                TypeCode.CBool
            )
                   ? null
                   : "difference";
        }
    }
}
