package fr.rthd.jlc.compiler;

import fr.rthd.jlc.env.FunType;

import java.util.List;

public abstract class InstructionBuilder {
    public abstract Instruction comment(String comment);

    public abstract Instruction noop();

    public abstract Instruction store(Variable dst, OperationItem src);

    public abstract Instruction load(Variable dst, Variable src);

    public abstract Instruction declare(Variable dst);

    public abstract Instruction functionDeclarationStart(FunType func);

    public abstract Instruction functionDeclarationEnd();

    public abstract Instruction declareExternalFunction(FunType func);

    public abstract Instruction call(
        String funcName,
        List<OperationItem> args
    );

    public abstract Instruction call(
        Variable dst,
        String funcName,
        List<OperationItem> args
    );

    public abstract Instruction label(String labelName);

    public abstract Instruction add(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction increment(
        Variable dst,
        Variable src
    );

    public abstract Instruction subtract(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction decrement(
        Variable dst,
        Variable src
    );

    public abstract Instruction multiply(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction divide(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction modulo(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction compare(
        Variable dst,
        OperationItem left,
        ComparisonOperator operator,
        OperationItem right
    );

    public abstract Instruction jump(String label);

    public abstract Instruction conditionalJump(
        OperationItem condition,
        String labelTrue,
        String labelFalse
    );

    public Instruction newLine() {
        return new Instruction("");
    }

    public abstract Instruction ret();

    public abstract Instruction ret(OperationItem returned);

    public abstract Instruction and(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction or(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    public abstract Instruction not(
        Variable dst,
        OperationItem src
    );

    public abstract Instruction neg(
        Variable dst,
        Variable src
    );

    public abstract Instruction globalStringLiteral(
        Variable global,
        String content
    );

    public abstract Instruction loadStringLiteral(
        Variable dst,
        Variable global
    );
}
