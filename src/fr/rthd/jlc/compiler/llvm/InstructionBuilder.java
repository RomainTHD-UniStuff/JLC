package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Instruction;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.internal.NotImplementedException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Instruction builder for LLVM
 * @author RomainTHD
 */
@NonNls
public class InstructionBuilder {
    /**
     * Output a new line
     * @return Instruction
     */
    @NotNull
    public Instruction newLine() {
        return new Instruction("");
    }

    /**
     * Output a comment
     * @param comment Comment to add
     * @return Instruction
     */
    @NotNull
    public Instruction comment(@NotNull String comment) {
        return new Instruction(
            String.format("; %s", comment)
        );
    }

    /**
     * No-op
     * @param noopLabel Label for a noop instruction
     * @return Instruction
     */
    @NotNull
    public Instruction noop(@NotNull String noopLabel) {
        // There is no noop in LLVM, so we make one ourselves by using a label
        //  and a jump to it, without any operation in between. The LLVM
        //  optimizer will hopefully be smart enough to remove this fake noop
        Instruction i = new Instruction();
        i.add(jump(noopLabel));
        i.add(label(noopLabel));
        return i;
    }

    /**
     * Store a value in a variable
     * @param dst Destination variable
     * @param src Value
     * @return Instruction
     */
    @NotNull
    public Instruction store(
        @NotNull Variable dst,
        @NotNull OperationItem src
    ) {
        return new Instruction(String.format(
            "store %s%s %s, %s%s %s",
            src.getType(),
            "*".repeat(src.getPointerLevel()),
            src,
            src.getType(),
            "*".repeat(dst.getPointerLevel()),
            dst
        ));
    }

    /**
     * Load a variable in memory to a temporary variable
     * @param dst Destination variable
     * @param src Source variable
     * @return Instruction
     */
    @NotNull
    public Instruction load(@NotNull Variable dst, @NotNull Variable src) {
        return new Instruction(String.format(
            "%s = load %s%s, %s%s %s",
            dst,
            dst.getType(),
            "*".repeat(dst.getPointerLevel()),
            dst.getType(),
            "*".repeat(dst.getPointerLevel() + 1),
            src
        ));
    }

    /**
     * Load a double pointer in memory to a single pointer variable
     * @param dst Destination variable
     * @param src Source variable
     * @return Instruction
     */
    @NotNull
    public Instruction loadDeref(@NotNull Variable dst, @NotNull Variable src) {
        return new Instruction(String.format(
            "%s = load %s*, %s** %s",
            dst,
            dst.getType(),
            dst.getType(),
            src
        ));
    }

    /**
     * Load a class attribute in memory
     * @param dst Destination variable
     * @param arg Attribute location
     * @return Instruction
     */
    @NotNull
    public Instruction loadAttribute(
        @NotNull Variable dst,
        @NotNull Variable thisVar,
        int arg
    ) {
        // FIXME: Ugly use of int
        return new Instruction(String.format(
            "%s = getelementptr %s, %s* %s, i32 0, i32 %d",
            dst,
            thisVar.getType(),
            thisVar.getType(),
            thisVar,
            arg
        ));
    }

    /**
     * Declare a new variable in memory
     * @param dst Destination variable
     * @return Instruction
     */
    @NotNull
    public Instruction declare(@NotNull Variable dst) {
        assert dst.getPointerLevel() != 0;
        return new Instruction(String.format(
            "%s = alloca %s%s",
            dst,
            dst.getType(),
            "*".repeat(dst.getPointerLevel() - 1)
        ));
    }

    /**
     * Start a function declaration
     * @param parentClass Parent class or null
     * @param retType Return type
     * @param funcName Function name
     * @param args Arguments
     * @return Instruction
     */
    @NotNull
    public Instruction functionDeclarationStart(
        @Nullable ClassType parentClass,
        @NotNull TypeCode retType,
        @NotNull String funcName,
        @NotNull List<Variable> args
    ) {
        return new Instruction(String.format(
            "define %s%s @%s(%s) nounwind \"nosync\" \"nofree\" {",
            retType,
            (retType.isPrimitive() ? "" : "*"),
            parentClass == null
            ? funcName
            : parentClass.getAssemblyMethodName(funcName),
            args.stream()
                .map(arg -> String.format(
                    "%s%s %%%s",
                    arg.getType(),
                    "*".repeat(arg.getPointerLevel()),
                    arg.getName()
                ))
                .reduce((a, b) -> String.format("%s, %s", a, b))
                .orElse("")
        ));
    }

    /**
     * End a function declaration
     * @return Instruction
     */
    @NotNull
    public Instruction functionDeclarationEnd() {
        return new Instruction("}");
    }

    /**
     * Declare an external function
     * @param func External function to declare
     * @return Instruction
     */
    @NotNull
    public Instruction declareExternalFunction(@NotNull FunType func) {
        return new Instruction(String.format(
            "declare %s @%s(%s)",
            func.getRetType(),
            func.getName(),
            func.getArgs()
                .stream()
                .map(arg -> String.format(
                    "%s %%%s",
                    arg.getType(),
                    arg.getName()
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
    @NotNull
    public Instruction call(
        @NotNull String funcName,
        @NotNull List<OperationItem> args
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
    @NotNull
    public Instruction call(
        @Nullable Variable dst,
        @NotNull String funcName,
        @NotNull List<OperationItem> args
    ) {
        return new Instruction(String.format(
            "%scall %s%s @%s(%s)",
            dst == null ? "" : dst + " = ",
            dst == null ? TypeCode.CVoid : dst.getType(),
            dst == null ? "" : "*".repeat(dst.getPointerLevel()),
            funcName,
            args.stream()
                .map(arg -> String.format(
                    "%s%s %s",
                    arg.getType(),
                    "*".repeat(arg.getPointerLevel()),
                    arg
                ))
                .reduce((a, b) -> String.format("%s, %s", a, b))
                .orElse("")
        ));
    }

    /**
     * Output a label
     * @param labelName Label name
     * @return Instruction
     */
    @NotNull
    public Instruction label(@NotNull String labelName) {
        Instruction i = new Instruction(String.format("%s:", labelName));
        i.setIndentable(false);
        return i;
    }

    /**
     * Add two values
     * @param dst Destination variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    @NotNull
    public Instruction add(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %sadd %s %s, %s",
            dst,
            left.getType() == TypeCode.CDouble ? "f" : "",
            left.getType(),
            left,
            right
        ));
    }

    /**
     * Increment a variable
     * @param src Input variable
     * @return Instruction
     */
    @NotNull
    public Instruction increment(@NotNull Variable src) {
        throw new NotImplementedException("Instruction not supported yet");
    }

    /**
     * Subtract two values
     * @param dst Destination variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    @NotNull
    public Instruction subtract(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %ssub %s %s, %s",
            dst,
            left.getType() == TypeCode.CDouble ? "f" : "",
            left.getType(),
            left,
            right
        ));
    }

    /**
     * Decrement a variable
     * @param src Input variable
     * @return Instruction
     */
    @NotNull
    public Instruction decrement(@NotNull Variable src) {
        throw new NotImplementedException("Instruction not supported yet");
    }

    /**
     * Multiply two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    @NotNull
    public Instruction multiply(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %smul %s %s, %s",
            dst,
            left.getType() == TypeCode.CDouble ? "f" : "",
            left.getType(),
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
    @NotNull
    public Instruction divide(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = %cdiv %s %s, %s",
            dst,
            left.getType() == TypeCode.CDouble ? 'f' : 's',
            left.getType(),
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
    @NotNull
    public Instruction modulo(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = srem %s %s, %s",
            dst,
            left.getType(),
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
    @NotNull
    public Instruction compare(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull ComparisonOperator operator,
        @NotNull OperationItem right
    ) {
        // Example: "%temp = fcmp oeq double %x, %y"
        return new Instruction(String.format(
            "%s = %ccmp %s %s%s %s, %s",
            dst,
            left.getType() == TypeCode.CDouble ? 'f' : 'i',
            ComparisonOperator.getOperand(operator, left.getType()),
            left.getType(),
            "*".repeat(left.getPointerLevel()),
            left,
            right
        ));
    }

    /**
     * Jump to a label
     * @param label Label name
     * @return Instruction
     */
    @NotNull
    public Instruction jump(@NotNull String label) {
        return new Instruction(String.format("br label %%%s", label));
    }

    /**
     * Conditional jump, for if statements or while statements
     * @param condition Condition value, boolean
     * @param labelTrue Label name if condition is true
     * @param labelFalse Label name if condition is false
     * @return Instruction
     */
    @NotNull
    public Instruction conditionalJump(
        @NotNull OperationItem condition,
        @NotNull String labelTrue,
        @NotNull String labelFalse
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
    @NotNull
    public Instruction ret() {
        return new Instruction("ret void");
    }

    /**
     * Non-void return
     * @param returned Returned value
     * @return Instruction
     */
    @NotNull
    public Instruction ret(@NotNull OperationItem returned) {
        return new Instruction(String.format(
            "ret %s%s %s",
            returned.getType(),
            "*".repeat(returned.getPointerLevel()),
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
    @NotNull
    public Instruction and(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = and %s %s, %s",
            dst,
            left.getType(),
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
    @NotNull
    public Instruction or(
        @NotNull Variable dst,
        @NotNull OperationItem left,
        @NotNull OperationItem right
    ) {
        return new Instruction(String.format(
            "%s = or %s %s, %s",
            dst,
            left.getType(),
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
    @NotNull
    public Instruction not(
        @NotNull Variable dst,
        @NotNull OperationItem src
    ) {
        return new Instruction(String.format(
            "%s = xor %s %s, 1",
            dst,
            src.getType(),
            src
        ));
    }

    /**
     * Mathematical negation of a value
     * @param dst Destination temp variable
     * @param src Source value
     * @return Instruction
     */
    @NotNull
    public Instruction neg(
        @NotNull Variable dst,
        @NotNull Variable src
    ) {
        if (src.getType() == TypeCode.CDouble) {
            return new Instruction(String.format(
                "%s = fneg %s %s",
                dst,
                src.getType(),
                src
            ));
        } else {
            return subtract(
                dst,
                new Literal(src.getType(), 0),
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
    @NotNull
    public Instruction globalStringLiteral(
        @NotNull Variable global,
        @NotNull String content
    ) {
        return new Instruction(String.format(
            "%s = private unnamed_addr constant [%d x i8] c\"%s\\00\", align 1",
            global,
            content.length() + 1,
            content.replace("\n", "\\0A")
        ));
    }

    /**
     * Load a string literal
     * @param dst Destination temp variable
     * @param global Global variable
     * @return Instruction
     */
    @NotNull
    public Instruction loadStringLiteral(
        @NotNull Variable dst,
        @NotNull Variable global
    ) {
        return new Instruction(String.format(
            "%s = getelementptr inbounds [%d x i8], [%d x i8]* %s, i32 0, i32 0",
            dst,
            global.getSize(),
            global.getSize(),
            global
        ));
    }

    /**
     * Class definition
     * @param className Class name
     * @param members Class members
     * @return Instruction
     */
    @NotNull
    public Instruction classDef(
        @NotNull String className,
        @NotNull List<TypeCode> members
    ) {
        return new Instruction(String.format(
            "%%%s = type { %s }",
            className,
            members.stream()
                   .map(t -> t.toString() + (t.isPrimitive() ? "" : "*"))
                   .collect(Collectors.joining(", "))
        ));
    }

    /**
     * `new` call, using malloc
     * @param dst Destination variable
     * @param tmp Temporary variable
     * @param c Class
     * @return Instruction
     */
    @NotNull
    public Instruction newObject(
        @NotNull Variable dst,
        @NotNull Variable tmp,
        @NotNull ClassType c
    ) {
        Instruction i = new Instruction();
        List<OperationItem> args = new ArrayList<>();
        args.add(new Literal(TypeCode.CInt, c.getSize()));
        i.add(call(tmp, "malloc", args));
        i.add(cast(dst, tmp, c.getName()));
        return i;
    }

    /**
     * Cast a value to a type
     * @param dst Destination variable
     * @param src Source value
     * @param className Class name
     * @return Instruction
     */
    @NotNull
    public Instruction cast(
        @NotNull Variable dst,
        @NotNull Variable src,
        @NotNull String className
    ) {
        return new Instruction(String.format(
            "%s = bitcast %s %s to %%%s*",
            dst,
            src.getType(),
            src,
            className
        ));
    }
}
