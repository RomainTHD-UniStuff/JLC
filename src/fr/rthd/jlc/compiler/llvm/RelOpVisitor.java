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

class RelOpVisitor implements RelOp.Visitor<OperationItem, EnvCompiler> {
    private final OperationItem _left;
    private final OperationItem _right;

    public RelOpVisitor(OperationItem left, OperationItem right) {
        _left = left;
        _right = right;
    }

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
