package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import javalette.Absyn.Void;

class ItemVisitor implements Item.Visitor<Void, EnvCompiler> {
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
        env.emit(env.instructionBuilder.declare(
            env.lookupVar(p.ident_)
        ));
        env.emit(env.instructionBuilder.store(
            env.lookupVar(p.ident_),
            AnnotatedExpr.getDefaultValue(type)
                         .accept(new ExprVisitor(), env)
        ));
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    public Void visit(Init p, EnvCompiler env) {
        Variable var = env.createVar(type, p.ident_, true);
        env.emit(env.instructionBuilder.declare(var));
        env.emit(env.instructionBuilder.store(
            var,
            p.expr_.accept(new ExprVisitor(), env)
        ));
        if (this.override) {
            env.updateVar(p.ident_, var);
        } else {
            env.insertVar(p.ident_, var);
        }
        env.emit(env.instructionBuilder.newLine());
        return null;
    }
}
