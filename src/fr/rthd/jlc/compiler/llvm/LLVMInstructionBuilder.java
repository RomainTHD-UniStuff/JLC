package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.ComparisonOperator;
import fr.rthd.jlc.compiler.Instruction;
import fr.rthd.jlc.compiler.InstructionBuilder;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.FunType;

import java.util.List;

public class LLVMInstructionBuilder extends InstructionBuilder {
    @Override
    public Instruction comment(String comment) {
        return new Instruction(
            String.format("; %s", comment)
        );
    }

    @Override
    public Instruction noop() {
        return new Instruction("nop");
    }

    @Override
    public Instruction store(Variable dst, OperationItem src) {
        return new Instruction(String.format(
            "store %s %s, %s* %s",
            src.type,
            src,
            src.type,
            dst
        ));
    }

    @Override
    public Instruction load(Variable dst, Variable src) {
        return new Instruction(String.format(
            "%s = load %s, %s* %s",
            dst,
            dst.type,
            dst.type,
            src
        ));
    }

    @Override
    public Instruction declare(Variable dst) {
        return new Instruction(String.format(
            "%s = alloca %s",
            dst,
            dst.type
        ));
    }

    @Override
    public Instruction functionDeclarationStart(FunType func) {
        return new Instruction(String.format(
            "define %s @%s(%s) {",
            func.retType,
            func.name,
            func.args.stream()
                     .map(arg -> String.format("%s %s", arg.type, arg))
                     .reduce((a, b) -> String.format("%s, %s", a, b))
                     .orElse("")
        ));
    }

    @Override
    public Instruction functionDeclarationEnd() {
        return new Instruction("}");
    }

    @Override
    public Instruction call(
        String funcName,
        List<Variable> args
    ) {
        return call(null, funcName, args);
    }

    @Override
    public Instruction call(
        Variable dst,
        String funcName,
        List<Variable> args
    ) {
        return new Instruction(String.format(
            "%scall %s @%s(%s)",
            dst == null ? "" : dst + " = ",
            dst == null ? TypeCode.CVoid : dst.type,
            funcName,
            args.stream()
                .map(arg -> String.format("%s %s", arg.type, arg))
                .reduce((a, b) -> String.format("%s, %s", a, b))
                .orElse("")
        ));
    }

    @Override
    public Instruction label(String labelName) {
        Instruction i = new Instruction(String.format("%s:", labelName));
        i.indentable = false;
        return i;
    }

    @Override
    public Instruction add(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %sadd %s %s, %s",
            dst,
            left.type == TypeCode.CDouble ? "f" : "",
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction increment(
        Variable dst,
        Variable src
    ) {
        return add(dst, src, new Literal(src.type, 1));
    }

    @Override
    public Instruction subtract(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %ssub %s %s, %s",
            dst,
            left.type == TypeCode.CDouble ? "f" : "",
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction decrement(
        Variable dst,
        Variable src
    ) {
        return subtract(dst, src, new Literal(src.type, 1));
    }

    @Override
    public Instruction multiply(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %smul %s %s, %s",
            dst,
            left.type == TypeCode.CDouble ? "f" : "",
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction divide(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %sdiv %s %s, %s",
            dst,
            left.type == TypeCode.CDouble ? "f" : "",
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction modulo(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = srem %s %s, %s",
            dst,
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction compare(
        Variable dst,
        OperationItem left,
        ComparisonOperator operator,
        OperationItem right
    ) {
        // Example: "%temp = fcmp oeq double %x, %y"
        return new Instruction(String.format(
            "%s = %ccmp %s %s %s, %s",
            dst,
            left.type == TypeCode.CDouble ? 'f' : 'i',
            LLVMComparisonOperator.getOperand(operator, left.type),
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction jump(String label) {
        return new Instruction(String.format("br label %%%s", label));
    }

    @Override
    public Instruction conditionalJump(
        OperationItem condition,
        String labelTrue,
        String labelFalse
    ) {
        return new Instruction(String.format(
            "br i1 %s, label %%%s, label %%%s",
            condition,
            labelTrue,
            labelFalse
        ));
    }

    @Override
    public Instruction ret() {
        // FIXME: Check if this is correct
        return new Instruction("ret void");
    }

    @Override
    public Instruction ret(OperationItem returned) {
        return new Instruction(String.format(
            "ret %s %s",
            returned.type,
            returned
        ));
    }

    @Override
    public Instruction and(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = and %s %s, %s",
            dst,
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction or(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = or %s %s, %s",
            dst,
            left.type,
            left,
            right
        ));
    }

    @Override
    public Instruction not(
        Variable dst,
        OperationItem src
    ) {
        return new Instruction(String.format(
            "%s = xor %s %s, 1",
            dst,
            src.type,
            src
        ));
    }

    @Override
    public Instruction neg(
        Variable dst,
        OperationItem src
    ) {
        if (src.type == TypeCode.CDouble) {
            return new Instruction(String.format(
                "%s = fneg %s %s",
                dst,
                src.type,
                src
            ));
        } else {
            return subtract(
                dst,
                new Literal(src.type, 0),
                src
            );
        }
    }
}
