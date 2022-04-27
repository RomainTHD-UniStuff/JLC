package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import javalette.Absyn.Void;

class ItemVisitor implements Item.Visitor<Void, EnvCompiler> {
    private final TypeCode _type;
    private final boolean _override;

    public ItemVisitor(TypeCode type) {
        this(type, false);
    }

    public ItemVisitor(TypeCode type, boolean override) {
        _type = type;
        _override = override;
    }

    public Void visit(NoInit p, EnvCompiler env) {
        env.insertVar(p.ident_, env.createVar(_type, p.ident_, true));
        env.emit(env.instructionBuilder.declare(
            env.lookupVar(p.ident_)
        ));
        if (_type.isPrimitive()) {
            env.emit(env.instructionBuilder.store(
                env.lookupVar(p.ident_),
                AnnotatedExpr.getDefaultValue(_type)
                             .accept(new ExprVisitor(), env)
            ));
        }
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    public Void visit(Init p, EnvCompiler env) {
        Variable var = env.createVar(_type, p.ident_, true);
        env.emit(env.instructionBuilder.declare(var));
        env.emit(env.instructionBuilder.store(
            var,
            p.expr_.accept(new ExprVisitor(), env)
        ));
        if (_override) {
            env.updateVar(p.ident_, var);
        } else {
            env.insertVar(p.ident_, var);
        }
        env.emit(env.instructionBuilder.newLine());
        return null;
    }
}
