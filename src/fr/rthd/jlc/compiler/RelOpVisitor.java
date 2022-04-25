package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.EQU;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.NE;
import javalette.Absyn.RelOp;

class RelOpVisitor implements RelOp.Visitor<OperationItem, EnvCompiler> {
    private final OperationItem left;
    private final OperationItem right;

    public RelOpVisitor(OperationItem left, OperationItem right) {
        this.left = left;
        this.right = right;
    }

    public OperationItem visit(LTH p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "lt");
        env.emit(env.instructionBuilder.compare(
            var,
            left,
            ComparisonOperator.LT,
            right
        ));
        return var;
    }

    public OperationItem visit(LE p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "le");
        env.emit(env.instructionBuilder.compare(
            var,
            left,
            ComparisonOperator.LE,
            right
        ));
        return var;
    }

    public OperationItem visit(GTH p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "gt");
        env.emit(env.instructionBuilder.compare(
            var,
            left,
            ComparisonOperator.GT,
            right
        ));
        return var;
    }

    public OperationItem visit(GE p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "ge");
        env.emit(env.instructionBuilder.compare(
            var,
            left,
            ComparisonOperator.GE,
            right
        ));
        return var;
    }

    public OperationItem visit(EQU p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "eq");
        env.emit(env.instructionBuilder.compare(
            var,
            left,
            ComparisonOperator.EQ,
            right
        ));
        return var;
    }

    public OperationItem visit(NE p, EnvCompiler env) {
        Variable var = env.createTempVar(TypeCode.CBool, "ne");
        env.emit(env.instructionBuilder.compare(
            var,
            left,
            ComparisonOperator.NE,
            right
        ));
        return var;
    }
}
