package fr.rthd.jlc.compiler;

import javalette.Absyn.AddOp;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;

class AddOpVisitor implements AddOp.Visitor<OperationItem, EnvCompiler> {
    private final OperationItem left;
    private final OperationItem right;

    public AddOpVisitor(OperationItem left, OperationItem right) {
        this.left = left;
        this.right = right;
    }

    public OperationItem visit(Plus p, EnvCompiler env) {
        Variable var = env.createTempVar(left.type, "add");
        env.emit(env.instructionBuilder.add(var, left, right));
        return var;
    }

    public OperationItem visit(Minus p, EnvCompiler env) {
        Variable var = env.createTempVar(left.type, "sub");
        env.emit(env.instructionBuilder.subtract(var, left, right));
        return var;
    }
}
