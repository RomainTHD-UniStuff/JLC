package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.AddOp;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Operator visitor for `+` and `-`
 * @author RomainTHD
 */
@NonNls
class AddOpVisitor implements AddOp.Visitor<OperationItem, EnvCompiler> {
    /**
     * Left operand
     */
    @NotNull
    private final OperationItem _left;

    /**
     * Right operand
     */
    @NotNull
    private final OperationItem _right;

    /**
     * Constructor
     * @param left left operand
     * @param right right operand
     */
    public AddOpVisitor(
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        _left = left;
        _right = right;
    }

    /**
     * Visit `+`
     * @param p `+`
     * @param env Environment
     * @return Result
     */
    @Override
    public OperationItem visit(Plus p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "add");
        env.emit(env.instructionBuilder.add(var, _left, _right));
        return var;
    }

    /**
     * Visit `-`
     * @param p `-`
     * @param env Environment
     * @return Result
     */
    @Override
    public OperationItem visit(Minus p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "sub");
        env.emit(env.instructionBuilder.subtract(var, _left, _right));
        return var;
    }
}
