package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import javalette.Absyn.Void;
import org.jetbrains.annotations.NotNull;

class ItemVisitor implements Item.Visitor<Void, EnvCompiler> {
    @NotNull
    private final TypeCode _type;
    private final boolean _override;

    public ItemVisitor(@NotNull TypeCode type) {
        this(type, false);
    }

    public ItemVisitor(@NotNull TypeCode type, boolean override) {
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
        if (_type.isPrimitive()) {
            env.emit(env.instructionBuilder.store(
                var,
                p.expr_.accept(new ExprVisitor(), env)
            ));
            if (_override) {
                env.updateVar(p.ident_, var);
            } else {
                env.insertVar(p.ident_, var);
            }
        } else {
            // FIXME: Ugly
            if (_override) {
                env.updateVar(p.ident_, var);
            } else {
                env.insertVar(p.ident_, var);
            }
            OperationItem value = p.expr_.accept(
                new ExprVisitor(p.ident_),
                env
            );
            if (value == null) {
                // Something like `A a = new A` where we actually call the
                //  constructor
                if (!var.getType().isObject()) {
                    throw new IllegalStateException(
                        "Error with assignment to object variable"
                    );
                }
            } else {
                env.emit(env.instructionBuilder.store(var, value));
            }
        }
        env.emit(env.instructionBuilder.newLine());
        return null;
    }
}
