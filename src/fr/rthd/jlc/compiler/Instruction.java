package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    private final List<String> _commands;

    private Instruction() {
        this._commands = new ArrayList<>();
    }

    private Instruction(String command) {
        this();
        this.add(command);
    }

    public static Instruction comment(String comment) {
        return new Instruction(String.format("; %s", comment));
    }

    public static Instruction noop() {
        return new Instruction("nop");
    }

    public static Instruction store(Variable dst, OperationItem src) {
        return new Instruction(String.format(
            "store %s %s, %s* %s",
            src.type,
            src,
            src.type,
            dst
        ));
    }

    public static Instruction load(Variable dst, Variable src) {
        return new Instruction(String.format(
            "%s = load %s, %s* %s",
            dst,
            dst.type,
            dst.type,
            src
        ));
    }

    public static Instruction declare(Variable dst) {
        return new Instruction(String.format(
            "%s = alloca %s",
            dst,
            dst.type
        ));
    }

    public static Instruction call(
        String funcName,
        List<Variable> args
    ) {
        return call(null, funcName, args);
    }

    public static Instruction call(
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

    public static Instruction label(String labelName) {
        return new Instruction(String.format("%s:", labelName));
    }

    public static Instruction add(
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

    public static Instruction subtract(
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

    public static Instruction multiply(
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

    public static Instruction divide(
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

    public static Instruction modulo(
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

    public static Instruction compare(
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
            operator.getOperand(left.type),
            left.type,
            left,
            right
        ));
    }

    public static Instruction jump(String label) {
        return new Instruction(String.format("br label %%%s", label));
    }

    public static Instruction conditionalJump(
        Variable condition,
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

    public static Instruction newLine() {
        return new Instruction("");
    }

    public static Instruction ret() {
        // FIXME: Check if this is correct
        return new Instruction("ret void");
    }

    public static Instruction ret(OperationItem returned) {
        return new Instruction(String.format(
            "ret %s %s",
            returned.type,
            returned
        ));
    }

    public static Instruction and(
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

    public static Instruction or(
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

    public static Instruction not(
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

    public static Instruction neg(
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
            return Instruction.subtract(
                dst,
                new Literal(src.type, 0),
                src
            );
        }
    }

    private void add(Instruction inst) {
        this._commands.addAll(inst.emit());
    }

    private void add(String command) {
        this._commands.add(command);
    }

    private void add(String command, int stackDifference) {
        this._commands.add(command);
    }

    public List<String> emit() {
        return _commands;
    }
}
