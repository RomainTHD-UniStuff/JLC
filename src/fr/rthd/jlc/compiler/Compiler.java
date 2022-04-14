package fr.rthd.jlc.compiler;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.NotImplementedException;
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
import javalette.Absyn.FnDef;
import javalette.Absyn.For;
import javalette.Absyn.FuncDef;
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
import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;
import javalette.Absyn.Type;
import javalette.Absyn.VRet;
import javalette.Absyn.While;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiler
 * @author RomainTHD
 */
public class Compiler {
    /**
     * Instruction builder
     */
    private static InstructionBuilder instructionBuilder;

    /**
     * Constructor
     * @param builder Instruction builder
     */
    public Compiler(InstructionBuilder builder) {
        instructionBuilder = builder;
    }

    /**
     * Javalette type from TypeCode
     * @param type TypeCode type
     * @return Javalette type
     * @throws IllegalArgumentException If type is not supported
     * @see TypeCode
     */
    private static Type javaletteTypeFromTypecode(TypeCode type) throws IllegalArgumentException {
        switch (type) {
            case CInt:
                return new javalette.Absyn.Int();

            case CDouble:
                return new javalette.Absyn.Doub();

            case CBool:
                return new javalette.Absyn.Bool();

            case CVoid:
                return new javalette.Absyn.Void();

            case CString:
            default:
                throw new IllegalArgumentException(
                    "Unsupported type: " +
                    type
                );
        }
    }

    /**
     * Entry point
     * @param p Program
     * @param parent Parent environment
     * @return Compiled program as a string
     */
    public String compile(Prog p, Env<?, FunType> parent) {
        EnvCompiler env = new EnvCompiler(parent);
        p.accept(new ProgVisitor(), env);
        return env.toAssembly();
    }

    public static class ProgVisitor implements Prog.Visitor<Void, EnvCompiler> {
        public Void visit(Program p, EnvCompiler env) {
            env.emit(instructionBuilder.newLine());

            for (FunType fun : env.getAllFun()) {
                if (fun.isExternal()) {
                    env.emit(instructionBuilder.declareExternalFunction(fun));
                }
            }

            env.emit(instructionBuilder.newLine());

            for (TopDef topdef : p.listtopdef_) {
                topdef.accept(new TopDefVisitor(), env);
            }
            return null;
        }
    }

    public static class FuncDefVisitor implements FuncDef.Visitor<Void, EnvCompiler> {
        public Void visit(FnDef p, EnvCompiler env) {
            FunType func = env.lookupFun(p.ident_);

            env.resetScope();

            func.args.forEach(arg -> {
                Variable var = env.createVar(arg.type, arg.name, false);
                env.insertVar(arg.name, var);
                arg.setGeneratedName(var.name);
            });

            env.emit(instructionBuilder.functionDeclarationStart(func));
            env.emit(instructionBuilder.label("entry"));

            func.args.forEach(arg -> new Init(
                arg.name,
                new EVar(arg.name)
            ).accept(new ItemVisitor(
                arg.type,
                true
            ), env));

            p.blk_.accept(new BlkVisitor(), env);

            if (func.retType == TypeCode.CVoid) {
                env.emit(instructionBuilder.ret());
            } else {
                env.emit(instructionBuilder.ret(new Literal(
                    func.retType,
                    func.retType.getDefaultValue()
                )));
            }

            env.emit(instructionBuilder.functionDeclarationEnd());
            env.emit(instructionBuilder.newLine());

            return null;
        }
    }

    public static class TopDefVisitor implements TopDef.Visitor<Void, EnvCompiler> {
        public Void visit(TopFnDef p, EnvCompiler env) {
            return p.funcdef_.accept(new FuncDefVisitor(), env);
        }

        public Void visit(TopClsDef p, EnvCompiler env) {
            throw new NotImplementedException();
        }
    }

    public static class ExprVisitor implements Expr.Visitor<OperationItem, EnvCompiler> {
        public OperationItem visit(EVar p, EnvCompiler env) {
            Variable var = env.lookupVar(p.ident_);
            if (var.isPointer()) {
                Variable tmp = env.createTempVar(var.type, String.format(
                    "var_%s",
                    var.name.replace(EnvCompiler.SEP, '-')
                ));
                env.emit(instructionBuilder.load(tmp, var));
                return tmp;
            } else {
                return var;
            }
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

        public OperationItem visit(ESelf p, EnvCompiler env) {
            throw new NotImplementedException();
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
            String content = p.string_;
            Variable global = env.createGlobalStringLiteral(content);

            if (env.lookupVar(global.name) == null) {
                // Avoid loading the same string literal multiple times
                env.insertVar(global.name, global);
                env.emitAtBeginning(instructionBuilder.globalStringLiteral(
                    global,
                    content
                ));
            }

            Variable tmp = env.createTempVar(
                TypeCode.CString,
                "string_literal"
            );
            env.emit(instructionBuilder.loadStringLiteral(tmp, global));
            return tmp;
        }

        public OperationItem visit(EDot p, EnvCompiler env) {
            throw new NotImplementedException();
        }

        public OperationItem visit(EIndex p, EnvCompiler env) {
            throw new NotImplementedException();
        }

        public OperationItem visit(ENew p, EnvCompiler env) {
            throw new NotImplementedException();
        }

        public OperationItem visit(Neg p, EnvCompiler env) {
            OperationItem expr = p.expr_.accept(new ExprVisitor(), env);
            if (expr instanceof Literal) {
                Literal lit = (Literal) expr;
                if (lit.type == TypeCode.CInt) {
                    return new Literal(TypeCode.CInt, -(int) lit.value);
                } else if (lit.type == TypeCode.CDouble) {
                    return new Literal(TypeCode.CDouble, -(double) lit.value);
                } else {
                    throw new RuntimeException("Unsupported type for negation");
                }
            } else {
                Variable var = env.createTempVar(expr.type, "neg");
                env.emit(instructionBuilder.neg(var, (Variable) expr));
                return var;
            }
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
            Variable var = env.createTempVar(TypeCode.CBool, "and_ptr", true);
            env.emit(instructionBuilder.declare(var));

            String trueLabel = env.getNewLabel("and_true");
            String falseLabel = env.getNewLabel("and_false");
            String endLabel = env.getNewLabel("and_end");

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
                var,
                p.expr_2.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.emit(instructionBuilder.label(falseLabel));
            env.emit(instructionBuilder.comment("and false"));
            env.emit(instructionBuilder.store(
                var,
                new Literal(TypeCode.CBool, false)
            ));
            env.emit(instructionBuilder.jump(endLabel));

            env.unindent();
            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endand"));
            env.emit(instructionBuilder.newLine());

            Variable tmp = env.createTempVar(var.type, "and");
            env.emit(instructionBuilder.load(tmp, var));
            return tmp;
        }

        public OperationItem visit(EOr p, EnvCompiler env) {
            Variable var = env.createTempVar(TypeCode.CBool, "or_ptr", true);
            env.emit(instructionBuilder.declare(var));

            String trueLabel = env.getNewLabel("or_true");
            String falseLabel = env.getNewLabel("or_false");
            String endLabel = env.getNewLabel("or_end");

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
                var,
                new Literal(TypeCode.CBool, true)
            ));
            env.emit(instructionBuilder.jump(endLabel));

            env.emit(instructionBuilder.label(falseLabel));
            env.enterScope();
            env.emit(instructionBuilder.comment("or false"));
            env.emit(instructionBuilder.store(
                var,
                p.expr_2.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.jump(endLabel));
            env.leaveScope();

            env.unindent();
            env.emit(instructionBuilder.label(endLabel));
            env.emit(instructionBuilder.comment("endor"));
            env.emit(instructionBuilder.newLine());

            Variable tmp = env.createTempVar(var.type, "or");
            env.emit(instructionBuilder.load(tmp, var));
            return tmp;
        }
    }

    public static class BlkVisitor implements Blk.Visitor<Void, EnvCompiler> {
        public Void visit(Block p, EnvCompiler env) {
            env.emit(instructionBuilder.comment("start block"));
            env.indent();

            env.enterScope();
            for (Stmt s : p.liststmt_) {
                s.accept(new StmtVisitor(), env);
            }
            env.leaveScope();

            env.unindent();
            env.emit(instructionBuilder.comment("end block"));
            return null;
        }
    }

    public static class StmtVisitor implements Stmt.Visitor<Void, EnvCompiler> {
        public Void visit(Empty p, EnvCompiler env) {
            String label = env.getNewLabel("noop");
            env.emit(instructionBuilder.jump(label));
            env.emit(instructionBuilder.label(label));
            return null;
        }

        public Void visit(BStmt p, EnvCompiler env) {
            p.blk_.accept(new BlkVisitor(), env);
            env.emit(instructionBuilder.newLine());
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
            Variable dst = env.lookupVar(p.ident_);

            if (!dst.isPointer()) {
                // We shouldn't be in this state
                throw new IllegalStateException(
                    "Assignment to non-pointer variable"
                );
            }

            env.emit(instructionBuilder.store(
                env.lookupVar(p.ident_),
                p.expr_.accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(Incr p, EnvCompiler env) {
            return new Ass(
                p.ident_,
                new EAdd(
                    new EVar(p.ident_),
                    new Plus(),
                    new ELitInt(1)
                )
            ).accept(new StmtVisitor(), env);
        }

        public Void visit(Decr p, EnvCompiler env) {
            return new Ass(
                p.ident_,
                new EAdd(
                    new EVar(p.ident_),
                    new Minus(),
                    new ELitInt(1)
                )
            ).accept(new StmtVisitor(), env);
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
            String thenLabel = env.getNewLabel("if_true");
            String endLabel = env.getNewLabel("if_end");

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
            String thenLabel = env.getNewLabel("if_true");
            String elseLabel = env.getNewLabel("if_false");
            String endLabel = env.getNewLabel("if_end");

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
            String cmpLabel = env.getNewLabel("while_compare");
            String loopLabel = env.getNewLabel("while_loop");
            String endLabel = env.getNewLabel("while_end");

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

        public Void visit(For p, EnvCompiler env) {
            throw new NotImplementedException();
        }

        public Void visit(SExp p, EnvCompiler env) {
            p.expr_.accept(new ExprVisitor(), env);
            return null;
        }
    }

    public static class ItemVisitor implements Item.Visitor<Void, EnvCompiler> {
        private final TypeCode type;
        private final boolean override;

        public ItemVisitor(TypeCode type) {
            this(type, false);
        }

        public ItemVisitor(TypeCode type, boolean override) {
            this.type = type;
            this.override = override;
        }

        public Void visit(NoInit p, EnvCompiler env) {
            env.insertVar(p.ident_, env.createVar(type, p.ident_, true));
            env.emit(instructionBuilder.declare(
                env.lookupVar(p.ident_)
            ));
            env.emit(instructionBuilder.store(
                env.lookupVar(p.ident_),
                AnnotatedExpr.getDefaultValue(type)
                             .accept(new ExprVisitor(), env)
            ));
            env.emit(instructionBuilder.newLine());
            return null;
        }

        public Void visit(Init p, EnvCompiler env) {
            Variable var = env.createVar(type, p.ident_, true);
            env.emit(instructionBuilder.declare(var));
            env.emit(instructionBuilder.store(
                var,
                p.expr_.accept(new ExprVisitor(), env)
            ));
            if (this.override) {
                env.updateVar(p.ident_, var);
            } else {
                env.insertVar(p.ident_, var);
            }
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
            Variable var = env.createTempVar(TypeCode.CBool, "lt");
            env.emit(instructionBuilder.compare(
                var,
                left,
                ComparisonOperator.LT,
                right
            ));
            return var;
        }

        public OperationItem visit(LE p, EnvCompiler env) {
            Variable var = env.createTempVar(TypeCode.CBool, "le");
            env.emit(instructionBuilder.compare(
                var,
                left,
                ComparisonOperator.LE,
                right
            ));
            return var;
        }

        public OperationItem visit(GTH p, EnvCompiler env) {
            Variable var = env.createTempVar(TypeCode.CBool, "gt");
            env.emit(instructionBuilder.compare(
                var,
                left,
                ComparisonOperator.GT,
                right
            ));
            return var;
        }

        public OperationItem visit(GE p, EnvCompiler env) {
            Variable var = env.createTempVar(TypeCode.CBool, "ge");
            env.emit(instructionBuilder.compare(
                var,
                left,
                ComparisonOperator.GE,
                right
            ));
            return var;
        }

        public OperationItem visit(EQU p, EnvCompiler env) {
            Variable var = env.createTempVar(TypeCode.CBool, "eq");
            env.emit(instructionBuilder.compare(
                var,
                left,
                ComparisonOperator.EQ,
                right
            ));
            return var;
        }

        public OperationItem visit(NE p, EnvCompiler env) {
            Variable var = env.createTempVar(TypeCode.CBool, "ne");
            env.emit(instructionBuilder.compare(
                var,
                left,
                ComparisonOperator.NE,
                right
            ));
            return var;
        }
    }
}
