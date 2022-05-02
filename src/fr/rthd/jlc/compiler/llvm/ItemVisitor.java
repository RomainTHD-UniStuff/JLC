package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Declaration visitor
 * @author RomainTHD
 */
@NonNls
class ItemVisitor implements Item.Visitor<Void, EnvCompiler> {
    /**
     * Variable type
     */
    @NotNull
    private final TypeCode _type;

    /**
     * Should override or not the variable. Used for function arguments, since
     * they are passed by value, and we need to do things like
     * `void f(x) { x++; }`
     */
    private final boolean _override;

    /**
     * Constructor
     * @param type Variable type
     */
    public ItemVisitor(@NotNull TypeCode type) {
        this(type, false);
    }

    /**
     * Constructor
     * @param type Variable type
     * @param override Should override or not
     */
    public ItemVisitor(@NotNull TypeCode type, boolean override) {
        _type = type;
        _override = override;
    }

    /**
     * Declaration only
     * @param p Declaration
     * @param env Environment
     */
    @Override
    public Void visit(NoInit p, EnvCompiler env) {
        env.insertVar(
            p.ident_,
            env.createVar(_type, p.ident_, _type.isPrimitive() ? 1 : 2)
        );
        // FIXME: Why lookup here?
        Variable v = env.lookupVar(p.ident_);
        assert v != null;
        env.emit(env.instructionBuilder.declare(v));
        if (_type.isPrimitive()) {
            // If primitive type, initialize with default value
            env.emit(env.instructionBuilder.store(
                v,
                AnnotatedExpr.getDefaultValue(_type)
                             .accept(new ExprVisitor(), env)
            ));
        }
        env.emit(env.instructionBuilder.newLine());
        return null;
    }

    /**
     * Declaration with initialization
     * @param p Declaration
     * @param env Environment
     */
    @Override
    public Void visit(Init p, EnvCompiler env) {
        Variable var = env.createVar(
            _type,
            p.ident_,
            _type.isPrimitive() ? 1 : 2
        );
        env.emit(env.instructionBuilder.declare(var));
        if (_type.isPrimitive()) {
            // If primitive type, initialize with default value and store after.
            //  Indeed, code like `int i = i;` where the right-hand side `i` is
            //  from a lower scope should be allowed.
            // FIXME: Is this really the case?
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
            // For objects, the variable needs to exist before the
            //  initialization so that the constructor call can be made
            if (_override) {
                env.updateVar(p.ident_, var);
            } else {
                env.insertVar(p.ident_, var);
            }
            OperationItem value = p.expr_.accept(new ExprVisitor(), env);

            if (value != null) {
                // FIXME: Something like `A a1 = a2` maybe?
                env.emit(env.instructionBuilder.store(var, value));
            }
        }
        env.emit(env.instructionBuilder.newLine());
        return null;
    }
}
