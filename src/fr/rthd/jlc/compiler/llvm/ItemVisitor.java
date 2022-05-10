package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Ass;
import javalette.Absyn.ELitInt;
import javalette.Absyn.ENew;
import javalette.Absyn.EVar;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.ListIndex;
import javalette.Absyn.NoInit;
import javalette.Absyn.SIndex;
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
        Variable v = env.createVar(
            _type,
            p.ident_,
            _type.isPrimitive() ? 1 : 2
        );
        // Objects are allowed to be null pointers. Arrays as well in
        //  theory, but to make it easier to use, we don't allow them
        env.insertVar(p.ident_, v);
        env.emit(env.instructionBuilder.declare(v));
        if (_type.isPrimitive()) {
            // If primitive type, initialize with default value
            env.emit(env.instructionBuilder.store(
                v,
                AnnotatedExpr.getDefaultValue(_type)
                             .accept(new ExprVisitor(), env)
            ));
        } else if (_type.isArray()) {
            // If array type, we create an empty array with length 0
            ListIndex indices = new ListIndex();
            indices.add(new SIndex(new ELitInt(0)));
            new Ass(
                new EVar(p.ident_),
                new ENew(
                    TypeVisitor.getTypeFromTypecode(_type).basetype_,
                    indices
                )
            ).accept(new StmtVisitor(), env);
        }
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
        OperationItem value = p.expr_.accept(new ExprVisitor(), env);

        OperationItem src;
        if (value.getType().equals(var.getType())) {
            src = value;
        } else {
            src = LLVMCompiler.castTo(var.getType(), value, env);
        }

        env.emit(env.instructionBuilder.store(var, src));
        if (_override) {
            env.updateVar(p.ident_, var);
        } else {
            env.insertVar(p.ident_, var);
        }
        return null;
    }
}
