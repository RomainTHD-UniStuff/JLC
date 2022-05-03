package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.EQU;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.NE;
import javalette.Absyn.RelOp;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Relation operator visitor
 * @author RomainTHD
 */
@NonNls
class RelOpVisitor implements RelOp.Visitor<OperationItem, EnvCompiler> {
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
    public RelOpVisitor(
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        _left = left;
        _right = right;
    }

    /**
     * Lower than
     * @param p Lower than
     * @param env Environment
     * @return Result variable
     */
    @Override
    public OperationItem visit(LTH p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "lt");
        env.emit(env.instructionBuilder.compare(
            var,
            _left,
            ComparisonOperator.LT,
            _right
        ));
        return var;
    }

    /**
     * Lower or equal
     * @param p Lower or equal
     * @param env Environment
     * @return Result variable
     */
    @Override
    public OperationItem visit(LE p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "le");
        env.emit(env.instructionBuilder.compare(
            var,
            _left,
            ComparisonOperator.LE,
            _right
        ));
        return var;
    }

    /**
     * Greater than
     * @param p Greater than
     * @param env Environment
     * @return Result variable
     */
    @Override
    public OperationItem visit(GTH p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "gt");
        env.emit(env.instructionBuilder.compare(
            var,
            _left,
            ComparisonOperator.GT,
            _right
        ));
        return var;
    }

    /**
     * Greater or equal
     * @param p Greater or equal
     * @param env Environment
     * @return Result variable
     */
    @Override
    public OperationItem visit(GE p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "ge");
        env.emit(env.instructionBuilder.compare(
            var,
            _left,
            ComparisonOperator.GE,
            _right
        ));
        return var;
    }

    /**
     * Equal
     * @param p Equal
     * @param env Environment
     * @return Result variable
     */
    @Override
    public OperationItem visit(EQU p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "eq");
        env.emit(env.instructionBuilder.compare(
            var,
            _left,
            ComparisonOperator.EQ,
            _right
        ));
        return var;
    }

    /**
     * Not equal
     * @param p Not equal
     * @param env Environment
     * @return Result variable
     */
    @Override
    public OperationItem visit(NE p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "ne");
        env.emit(env.instructionBuilder.compare(
            var,
            _left,
            ComparisonOperator.NE,
            _right
        ));
        return var;
    }
}
