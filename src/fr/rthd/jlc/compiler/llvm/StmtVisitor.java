package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.internal.NotImplementedException;
import javalette.Absyn.Ass;
import javalette.Absyn.BStmt;
import javalette.Absyn.Cond;
import javalette.Absyn.CondElse;
import javalette.Absyn.Decl;
import javalette.Absyn.Decr;
import javalette.Absyn.EAdd;
import javalette.Absyn.ELitInt;
import javalette.Absyn.EVar;
import javalette.Absyn.Empty;
import javalette.Absyn.For;
import javalette.Absyn.Incr;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;
import javalette.Absyn.Ret;
import javalette.Absyn.SExp;
import javalette.Absyn.Stmt;
import javalette.Absyn.VRet;
import javalette.Absyn.Void;
import javalette.Absyn.While;
import org.jetbrains.annotations.NonNls;

/**
 * Statement visitor
 * @author RomainTHD
 */
@NonNls
class StmtVisitor implements Stmt.Visitor<Void, EnvCompiler> {
    /**
     * Empty statement
     * @param p Empty statement
     * @param env Environment
     */
    @Override
    public Void visit(Empty p, EnvCompiler env) {
        String label = env.getNewLabel("noop");
        env.emit(env.instructionBuilder.jump(label));
        env.emit(env.instructionBuilder.label(label));
        return null;
    }

    /**
     * Block
     * @param p Block
     * @param env Environment
     */
    @Override
    public Void visit(BStmt p, EnvCompiler env) {
        p.blk_.accept(new BlkVisitor(), env);
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * Variable declaration
     * @param p Variable declaration
     * @param env Environment
     */
    @Override
    public Void visit(Decl p, EnvCompiler env) {
        p.listitem_.forEach(item -> item.accept(
            new ItemVisitor(p.type_.accept(new TypeVisitor(), null)),
            env
        ));
        return null;
    }

    /**
     * Assignment
     * @param p Assignment
     * @param env Environment
     */
    @Override
    public Void visit(Ass p, EnvCompiler env) {
        Variable dst = env.lookupVar(p.ident_);
        assert dst != null;
        assert dst.isPointer();

        OperationItem value = p.expr_.accept(new ExprVisitor(p.ident_), env);
        if (value == null) {
            // The value can only be null if the variable is an object, like
            //  `a = new A`, because we actually only call the constructor
            assert dst.getType().isObject();
        } else {
            env.emit(env.instructionBuilder.store(
                env.lookupVar(p.ident_),
                value
            ));
        }

        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * Increment
     * @param p Increment
     * @param env Environment
     */
    @Override
    public Void visit(Incr p, EnvCompiler env) {
        // TODO: Use a real increment instruction
        return new Ass(
            p.ident_,
            new EAdd(
                new EVar(p.ident_),
                new Plus(),
                new ELitInt(1)
            )
        ).accept(new StmtVisitor(), env);
    }

    /**
     * Decrement
     * @param p Decrement
     * @param env Environment
     */
    @Override
    public Void visit(Decr p, EnvCompiler env) {
        // TODO: Use a real decrement instruction
        return new Ass(
            p.ident_,
            new EAdd(
                new EVar(p.ident_),
                new Minus(),
                new ELitInt(1)
            )
        ).accept(new StmtVisitor(), env);
    }

    /**
     * Non-void return
     * @param p Non-void return
     * @param env Environment
     */
    @Override
    public Void visit(Ret p, EnvCompiler env) {
        env.emit(env.instructionBuilder.ret(
            p.expr_.accept(new ExprVisitor(), env)
        ));
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * Void return
     * @param p Void return
     * @param env Environment
     */
    @Override
    public Void visit(VRet p, EnvCompiler env) {
        env.emit(env.instructionBuilder.ret());
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * If
     * @param p If
     * @param env Environment
     */
    @Override
    public Void visit(Cond p, EnvCompiler env) {
        String thenLabel = env.getNewLabel("if_true");
        String endLabel = env.getNewLabel("if_end");

        env.emit(env.instructionBuilder.comment("if"));
        env.indent();
        env.emit(env.instructionBuilder.comment("if exp"));

        OperationItem res = p.expr_.accept(new ExprVisitor(), env);
        env.emit(env.instructionBuilder.conditionalJump(
            res,
            thenLabel,
            endLabel
        ));

        env.emit(env.instructionBuilder.label(thenLabel));
        env.enterScope();
        env.emit(env.instructionBuilder.comment("if then"));
        p.stmt_.accept(new StmtVisitor(), env);
        // Not useful to emit a jump here since there is a fallthrough
        env.emit(env.instructionBuilder.jump(endLabel));
        env.leaveScope();

        env.unindent();
        env.emit(env.instructionBuilder.label(endLabel));
        env.emit(env.instructionBuilder.comment("endif"));

        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * If-else
     * @param p If-else
     * @param env Environment
     */
    @Override
    public Void visit(CondElse p, EnvCompiler env) {
        String thenLabel = env.getNewLabel("if_true");
        String elseLabel = env.getNewLabel("if_false");
        String endLabel = env.getNewLabel("if_end");

        env.emit(env.instructionBuilder.comment("if"));
        env.indent();
        env.emit(env.instructionBuilder.comment("if exp"));

        OperationItem res = p.expr_.accept(new ExprVisitor(), env);
        env.emit(env.instructionBuilder.conditionalJump(
            res,
            thenLabel,
            elseLabel
        ));

        env.emit(env.instructionBuilder.label(thenLabel));
        env.enterScope();
        env.emit(env.instructionBuilder.comment("if then"));
        p.stmt_1.accept(new StmtVisitor(), env);
        env.emit(env.instructionBuilder.jump(endLabel));
        env.leaveScope();

        env.emit(env.instructionBuilder.label(elseLabel));
        env.enterScope();
        env.emit(env.instructionBuilder.comment("if else"));
        p.stmt_2.accept(new StmtVisitor(), env);
        // Not useful to emit a jump here since there is a fallthrough
        env.emit(env.instructionBuilder.jump(endLabel));
        env.leaveScope();

        env.unindent();
        env.emit(env.instructionBuilder.label(endLabel));
        env.emit(env.instructionBuilder.comment("endif"));

        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * While
     * @param p While
     * @param env Environment
     */
    @Override
    public Void visit(While p, EnvCompiler env) {
        String cmpLabel = env.getNewLabel("while_compare");
        String loopLabel = env.getNewLabel("while_loop");
        String endLabel = env.getNewLabel("while_end");

        env.emit(env.instructionBuilder.comment("while"));
        env.emit(env.instructionBuilder.jump(cmpLabel));
        env.indent();
        env.emit(env.instructionBuilder.label(cmpLabel));
        env.emit(env.instructionBuilder.comment("while exp"));

        OperationItem res = p.expr_.accept(new ExprVisitor(), env);
        env.emit(env.instructionBuilder.conditionalJump(
            res,
            loopLabel,
            endLabel
        ));

        env.emit(env.instructionBuilder.label(loopLabel));
        env.enterScope();
        env.emit(env.instructionBuilder.comment("while loop"));
        p.stmt_.accept(new StmtVisitor(), env);
        env.emit(env.instructionBuilder.jump(cmpLabel));
        env.leaveScope();
        env.unindent();

        env.emit(env.instructionBuilder.label(endLabel));
        env.emit(env.instructionBuilder.comment("endwhile"));

        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * For
     * @param p For
     * @param env Environment
     */
    @Override
    public Void visit(For p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    /**
     * Expression as statement, like `f();
     * @param p Expression
     * @param env Environment
     */
    @Override
    public Void visit(SExp p, EnvCompiler env) {
        p.expr_.accept(new ExprVisitor(), env);
        return null;
    }
}
