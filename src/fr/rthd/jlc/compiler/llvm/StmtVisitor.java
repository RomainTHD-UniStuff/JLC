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

class StmtVisitor implements Stmt.Visitor<Void, EnvCompiler> {
    public Void visit(Empty p, EnvCompiler env) {
        String label = env.getNewLabel("noop");
        env.emit(env.instructionBuilder.jump(label));
        env.emit(env.instructionBuilder.label(label));
        return null;
    }

    public Void visit(BStmt p, EnvCompiler env) {
        p.blk_.accept(new BlkVisitor(), env);
        env.emit(env.instructionBuilder.newLine());
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

        env.emit(env.instructionBuilder.store(
            env.lookupVar(p.ident_),
            p.expr_.accept(new ExprVisitor(), env)
        ));
        env.emit(env.instructionBuilder.newLine());
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
        env.emit(env.instructionBuilder.ret(
            p.expr_.accept(new ExprVisitor(), env)
        ));
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    public Void visit(VRet p, EnvCompiler env) {
        env.emit(env.instructionBuilder.ret());
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

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

    public Void visit(For p, EnvCompiler env) {
        throw new NotImplementedException();
    }

    public Void visit(SExp p, EnvCompiler env) {
        p.expr_.accept(new ExprVisitor(), env);
        return null;
    }
}
