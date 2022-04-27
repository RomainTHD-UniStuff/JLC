package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Instruction;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Instruction builder for LLVM
 * @author RomainTHD
 */
public class InstructionBuilder {
    /**
     * Output a new line
     * @return Instruction
     */
    public Instruction newLine() {
        return new Instruction("");
    }

    /**
     * Output a comment
     * @param comment Comment to add
     * @return Instruction
     */
    public Instruction comment(String comment) {
        return new Instruction(
            String.format("; %s", comment)
        );
    }

    /**
     * No-op
     * @return Instruction
     */
    public Instruction noop() {
        throw new UnsupportedOperationException("Noop is not supported by LLVM");
    }

    /**
     * Store a value in a variable
     * @param dst Destination variable
     * @param src Value
     * @return Instruction
     */
    public Instruction store(Variable dst, OperationItem src) {
        return new Instruction(String.format(
            "store %s %s, %s* %s",
            src.type,
            src,
            src.type,
            dst
        ));
    }

    /**
     * Load a variable in memory to a temporary variable
     * @param dst Destination variable
     * @param src Source variable
     * @return Instruction
     */
    public Instruction load(Variable dst, Variable src) {
        return new Instruction(String.format(
            "%s = load %s, %s* %s",
            dst,
            dst.type,
            dst.type,
            src
        ));
    }

    /**
     * Load a class attribute in memory
     * @param dst Destination variable
     * @param arg Attribute location
     * @return Instruction
     */
    public Instruction loadAttribute(
        Variable dst,
        Variable thisVar,
        int arg
    ) {
        // FIXME: Ugly use of int
        return new Instruction(String.format(
            "%s = getelementptr %s, %s* %s, i32 0, i32 %d",
            dst,
            thisVar.type,
            thisVar.type,
            thisVar,
            arg
        ));
    }

    /**
     * Declare a new variable in memory
     * @param dst Destination variable
     * @return Instruction
     */
    public Instruction declare(Variable dst) {
        return new Instruction(String.format(
            "%s = alloca %s",
            dst,
            dst.type
        ));
    }

    /**
     * Start a function declaration
     * @param parentClass Parent class or null
     * @param func Function to declare
     * @return Instruction
     */
    public Instruction functionDeclarationStart(
        ClassType parentClass,
        FunType func
    ) {
        return new Instruction(String.format(
            "define %s @%s(%s) nounwind \"nosync\" \"nofree\" {",
            func.retType,
            (parentClass == null ? "" : parentClass.name + "$") + func.name,
            func.getArgs()
                .stream()
                .map(arg -> String.format(
                    "%s%s %%%s",
                    arg.type,
                    arg.type.isPrimitive() ? "" : "*",
                    // FIXME: Shouldn't be used, will not work with
                    //  primitive pointers
                    arg.getGeneratedName()
                ))
                .reduce((a, b) -> String.format("%s, %s", a, b))
                .orElse("")
        ));
    }

    /**
     * End a function declaration
     * @return Instruction
     */
    public Instruction functionDeclarationEnd() {
        return new Instruction("}");
    }

    /**
     * Declare an external function
     * @param func External function to declare
     * @return Instruction
     */
    public Instruction declareExternalFunction(FunType func) {
        return new Instruction(String.format(
            "declare %s @%s(%s)",
            func.retType,
            func.name,
            func.getArgs()
                .stream()
                .map(arg -> String.format(
                    "%s %%%s",
                    arg.type,
                    arg.getGeneratedName()
                ))
                .reduce((a, b) -> String.format("%s, %s", a, b))
                .orElse("")
        ));
    }

    /**
     * Call a void function
     * @param funcName Function name
     * @param args Arguments
     * @return Instruction
     */
    public Instruction call(
        String funcName,
        List<OperationItem> args
    ) {
        return call(null, funcName, args);
    }

    /**
     * Call a non-void function
     * @param dst Destination variable for return value
     * @param funcName Function name
     * @param args Arguments
     * @return Instruction
     */
    public Instruction call(
        Variable dst,
        String funcName,
        List<OperationItem> args
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

    public Instruction constructor(
        Variable var,
        ClassType c
    ) {
        return call(
            c.getConstructorName(),
            Collections.singletonList(var)
        );
    }

    /**
     * Output a label
     * @param labelName Label name
     * @return Instruction
     */
    public Instruction label(String labelName) {
        Instruction i = new Instruction(String.format("%s:", labelName));
        i.indentable = false;
        return i;
    }

    /**
     * Add two values
     * @param dst Destination variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
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

    /**
     * Increment a variable
     * @param dst Destination temp variable
     * @param src Input variable
     * @return Instruction
     */
    public Instruction increment(
        Variable dst,
        Variable src
    ) {
        return add(dst, src, new Literal(src.type, 1));
    }

    /**
     * Subtract two values
     * @param dst Destination variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
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

    /**
     * Decrement a variable
     * @param dst Destination temp variable
     * @param src Input variable
     * @return Instruction
     */
    public Instruction decrement(
        Variable dst,
        Variable src
    ) {
        return subtract(dst, src, new Literal(src.type, 1));
    }

    /**
     * Multiply two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
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

    /**
     * Divide two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public Instruction divide(
        Variable dst,
        OperationItem left,
        OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %cdiv %s %s, %s",
            dst,
            left.type == TypeCode.CDouble ? 'f' : 's',
            left.type,
            left,
            right
        ));
    }

    /**
     * Modulo two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
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

    /**
     * Compare two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param operator Operator
     * @param right Right value
     * @return Instruction
     */
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
            ComparisonOperator.getOperand(operator, left.type),
            left.type,
            left,
            right
        ));
    }

    /**
     * Jump to a label
     * @param label Label name
     * @return Instruction
     */
    public Instruction jump(String label) {
        return new Instruction(String.format("br label %%%s", label));
    }

    /**
     * Conditional jump, for if statements or while statements
     * @param condition Condition value, boolean
     * @param labelTrue Label name if condition is true
     * @param labelFalse Label name if condition is false
     * @return Instruction
     */
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

    /**
     * Void return
     * @return Instruction
     */
    public Instruction ret() {
        return new Instruction("ret void");
    }

    /**
     * Non-void return
     * @param returned Returned value
     * @return Instruction
     */
    public Instruction ret(OperationItem returned) {
        return new Instruction(String.format(
            "ret %s %s",
            returned.type,
            returned
        ));
    }

    /**
     * AND two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
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

    /**
     * OR two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
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

    /**
     * Logic negation of a value
     * @param dst Destination temp variable
     * @param src Source value
     * @return Instruction
     */
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

    /**
     * Mathematical negation of a value
     * @param dst Destination temp variable
     * @param src Source value
     * @return Instruction
     */
    public Instruction neg(
        Variable dst,
        Variable src
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

    /**
     * Global string literal
     * @param global Global variable
     * @param content String content
     * @return Instruction
     */
    public Instruction globalStringLiteral(
        Variable global,
        String content
    ) {
        return new Instruction(String.format(
            "%s = private unnamed_addr constant [%d x i8] c\"%s\\00\", align 1",
            global,
            content.length() + 1,
            content.replace("\n", "\\0A")
        ));
    }

    public Instruction loadStringLiteral(
        Variable dst,
        Variable global
    ) {
        return new Instruction(String.format(
            "%s = getelementptr inbounds [%d x i8], [%d x i8]* %s, i32 0, i32 0",
            dst,
            global.getSize(),
            global.getSize(),
            global
        ));
    }

    public Instruction classDef(String className, List<TypeCode> members) {
        return new Instruction(String.format(
            "%%%s = type { %s }",
            className,
            members.stream()
                   .map(TypeCode::toString)
                   .collect(Collectors.joining(", "))
        ));
    }
}
