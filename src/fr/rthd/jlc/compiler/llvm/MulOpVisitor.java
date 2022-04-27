package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Div;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.Times;

class MulOpVisitor implements MulOp.Visitor<OperationItem, EnvCompiler> {
    private final OperationItem _left;
    private final OperationItem _right;

    public MulOpVisitor(OperationItem left, OperationItem right) {
        _left = left;
        _right = right;
    }

    public OperationItem visit(Times p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "mult");
        env.emit(env.instructionBuilder.multiply(var, _left, _right));
        return var;
    }

    public OperationItem visit(Div p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "div");
        env.emit(env.instructionBuilder.divide(var, _left, _right));
        return var;
    }

    public OperationItem visit(Mod p, EnvCompiler env) {
        Variable var = env.createTempVar(_left.getType(), "mod");
        env.emit(env.instructionBuilder.modulo(var, _left, _right));
        return var;
    }
}
