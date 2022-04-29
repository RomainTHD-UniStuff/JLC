package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Div;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.Times;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Operator visitor for `*`, `/` and `%`
 * @author RomainTHD
 */
@NonNls
class MulOpVisitor implements MulOp.Visitor<OperationItem, EnvCompiler> {
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
     * @param left Left operand
     * @param right Right operand
     */
    public MulOpVisitor(
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        _left = left;
        _right = right;
    }

    /**
     * Visit `*` operator
     * @param p `*` operator
     * @param env Environment
     * @return Result
     */
    @Override
    public OperationItem visit(Times p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "mult");
        env.emit(env.instructionBuilder.multiply(var, _left, _right));
        return var;
    }

    /**
     * Visit `/` operator
     * @param p `/` operator
     * @param env Environment
     * @return Result
     */
    @Override
    public OperationItem visit(Div p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "div");
        env.emit(env.instructionBuilder.divide(var, _left, _right));
        return var;
    }

    /**
     * Visit `%` operator
     * @param p `%` operator
     * @param env Environment
     * @return Result
     */
    @Override
    public OperationItem visit(Mod p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "mod");
        env.emit(env.instructionBuilder.modulo(var, _left, _right));
        return var;
    }
}
