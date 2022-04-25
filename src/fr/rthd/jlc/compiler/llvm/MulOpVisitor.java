package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import javalette.Absyn.Div;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.Times;

class MulOpVisitor implements MulOp.Visitor<OperationItem, EnvCompiler> {
    private final OperationItem left;
    private final OperationItem right;

    public MulOpVisitor(OperationItem left, OperationItem right) {
        this.left = left;
        this.right = right;
    }

    public OperationItem visit(Times p, EnvCompiler env) {
        Variable var = env.createTempVar(left.type, "mult");
        env.emit(env.instructionBuilder.multiply(var, left, right));
        return var;
    }

    public OperationItem visit(Div p, EnvCompiler env) {
        Variable var = env.createTempVar(left.type, "div");
        env.emit(env.instructionBuilder.divide(var, left, right));
        return var;
    }

    public OperationItem visit(Mod p, EnvCompiler env) {
        Variable var = env.createTempVar(left.type, "mod");
        env.emit(env.instructionBuilder.modulo(var, left, right));
        return var;
    }
}
