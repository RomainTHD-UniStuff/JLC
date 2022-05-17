package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Instruction;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.OperationItem;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Instruction builder for LLVM. Quite critical section, so `String.format`
 * shouldn't be used, as it is several magnitudes slower.
 * @author RomainTHD
 */
@NonNls
class InstructionBuilder {
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
        return new Instruction("; " + comment);
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
        @NotNull OperationItem dst,
        @NotNull OperationItem src
    ) {
        // "store %s%s %s, %s%s %s"
        return new Instruction(
            "store "
            + src.getType()
            + "*".repeat(src.getPointerLevel())
            + " "
            + src
            + ", "
            + src.getType()
            + "*".repeat(dst.getPointerLevel())
            + " "
            + dst
        );
    }

    /**
     * Load a variable in memory to a temporary variable
     * @param dst Destination variable
     * @param src Source variable
     * @return Instruction
     */
    @NotNull
    public Instruction load(@NotNull Variable dst, @NotNull Variable src) {
        // "%s = load %s%s, %s%s %s"
        return new Instruction(
            dst
            + " = load "
            + dst.getType()
            + "*".repeat(dst.getPointerLevel())
            + ", "
            + dst.getType()
            + "*".repeat(dst.getPointerLevel() + 1)
            + " "
            + src
        );
    }

    /**
     * Load a class attribute in memory
     * @param dst Destination variable
     * @param attributeLocation Attribute location
     * @return Instruction
     */
    @NotNull
    public Instruction loadAttribute(
        @NotNull Variable dst,
        @NotNull OperationItem thisVar,
        int attributeLocation
    ) {
        // "%s = getelementptr %s, %s%s %s, i32 0, i32 %d"
        return new Instruction(
            dst
            + " = getelementptr "
            + thisVar.getType()
            + ", "
            + thisVar.getType()
            + "*".repeat(thisVar.getPointerLevel())
            + ""
            + thisVar
            + ", i32 0, i32 "
            + attributeLocation
        );
    }

    /**
     * Declare a new variable in memory
     * @param dst Destination variable
     * @return Instruction
     */
    @NotNull
    public Instruction declare(@NotNull Variable dst) {
        // "%s = alloca %s%s"
        assert dst.getPointerLevel() != 0;
        return new Instruction(
            dst
            + " = alloca "
            + dst.getType()
            + "*".repeat(dst.getPointerLevel() - 1)
        );
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
        @Nullable ClassType<?> parentClass,
        @NotNull TypeCode retType,
        @NotNull String funcName,
        @NotNull List<Variable> args
    ) {
        // "define %s%s @%s(%s) nounwind \"nosync\" \"nofree\" {",
        return new Instruction(
            "define "
            + retType
            + (retType.isPrimitive() ? "" : "*")
            + " @"
            + (
                parentClass == null
                ? funcName
                : parentClass.getAssemblyMethodName(funcName)
            )
            + "("
            + args.stream()
                  // "%s%s %%%s"
                  .map(arg -> arg.getType()
                              + "*".repeat(arg.getPointerLevel())
                              + " %"
                              + arg.getName()
                  )
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("")
            + ") nounwind \"nosync\" \"nofree\" {"
        );
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
        // "declare %s @%s(%s)"
        return new Instruction(
            "declare "
            + func.getRetType()
            + " @"
            + func.getName()
            + "("
            + func.getArgs()
                  .stream()
                  // "%s %%%s"
                  .map(arg -> arg.getType() + " %" + arg.getName())
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("")
            + ")"
        );
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
        // "%scall %s%s @%s(%s)"
        return new Instruction(
            (dst == null ? "" : dst + " = ")
            + "call "
            + (dst == null ? TypeCode.CVoid : dst.getType())
            + (dst == null ? "" : "*".repeat(dst.getPointerLevel()))
            + " @"
            + funcName
            + "("
            + args.stream()
                  // "%s%s %s",
                  .map(arg -> arg.getType()
                              + "*".repeat(arg.getPointerLevel())
                              + " "
                              + arg
                  )
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("")
            + ")"
        );
    }

    /**
     * Output a label
     * @param labelName Label name
     * @return Instruction
     */
    @NotNull
    public Instruction label(@NotNull String labelName) {
        Instruction i = new Instruction(labelName + ":");
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
        // "%s = %sadd %s %s, %s"
        return new Instruction(
            dst
            + " = "
            + (left.getType() == TypeCode.CDouble ? "f" : "")
            + "add "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = %ssub %s %s, %s"
        return new Instruction(
            dst
            + " = "
            + (left.getType() == TypeCode.CDouble ? "f" : "")
            + "sub "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = %smul %s %s, %s"
        return new Instruction(
            dst
            + " = "
            + (left.getType() == TypeCode.CDouble ? "f" : "")
            + "mul "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = %cdiv %s %s, %s"
        return new Instruction(
            dst
            + " = "
            + (left.getType() == TypeCode.CDouble ? "f" : "s")
            + "div "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = srem %s %s, %s"
        return new Instruction(
            dst
            + " = "
            + "srem "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = %ccmp %s %s%s %s, %s"
        // Example: "%temp = fcmp oeq double %x, %y"
        return new Instruction(
            dst
            + " = "
            + (left.getType() == TypeCode.CDouble ? 'f' : 'i')
            + "cmp "
            + ComparisonOperator.getOperand(operator, left.getType())
            + " "
            + left.getType()
            + "*".repeat(left.getPointerLevel())
            + " "
            + left
            + ", "
            + right
        );
    }

    /**
     * Jump to a label
     * @param label Label name
     * @return Instruction
     */
    @NotNull
    public Instruction jump(@NotNull String label) {
        return new Instruction("br label %" + label);
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
        // "br i1 %s, label %%%s, label %%%s"
        return new Instruction(
            "br i1 "
            + condition
            + ", label %"
            + labelTrue
            + ", label %"
            + labelFalse
        );
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
        // "ret %s%s %s"
        return new Instruction(
            "ret "
            + returned.getType()
            + "*".repeat(returned.getPointerLevel())
            + " "
            + returned
        );
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
        // "%s = and %s %s, %s"
        return new Instruction(
            dst
            + " = and "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = or %s %s, %s"
        return new Instruction(
            dst
            + " = or "
            + left.getType()
            + " "
            + left
            + ", "
            + right
        );
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
        // "%s = xor %s %s, 1"
        return new Instruction(
            dst
            + " = xor "
            + src.getType()
            + " "
            + src
            + ", 1"
        );
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
            // "%s = fneg %s %s"
            return new Instruction(
                dst
                + " = fneg "
                + src.getType()
                + " "
                + src
            );
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
        // "%s = private unnamed_addr constant [%d x i8] c\"%s\\00\", align 1"
        return new Instruction(
            global
            + " = private unnamed_addr constant ["
            + (content.length() + 1)
            + " x i8] c\""
            + content.replace("\n", "\\0A")
            + "\\00\", align 1"
        );
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
        // "%s = getelementptr inbounds [%d x i8], [%d x i8]* %s, i32 0, i32 0"
        return new Instruction(
            dst
            + " = getelementptr inbounds ["
            + global.getSize()
            + " x i8], ["
            + global.getSize()
            + " x i8]* "
            + global
            + ", i32 0, i32 0"
        );
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
        // "%%%s = type { %s }"
        return new Instruction(
            "%"
            + className
            + " = type { "
            + members.stream()
                     .map(t -> t.toString() + (t.isPrimitive() ? "" : "*"))
                     .collect(Collectors.joining(", "))
            + " }"
        );
    }

    /**
     * `new` call, using malloc
     * @param dst Destination variable
     * @param tmp Temporary variable
     * @param size Size of the object to allocate
     * @return Instruction
     */
    @NotNull
    public Instruction newObject(
        @NotNull Variable dst,
        @NotNull Variable tmp,
        int size
    ) {
        Instruction i = new Instruction();
        List<OperationItem> args = new ArrayList<>();
        args.add(new Literal(TypeCode.CInt, size));
        i.add(call(tmp, "malloc", args));
        i.add(cast(dst, tmp, dst.getType()));
        return i;
    }

    /**
     * Cast a value to a type
     * @param dst Destination variable
     * @param src Source value
     * @param classType Class type
     * @return Instruction
     */
    @NotNull
    public Instruction cast(
        @NotNull Variable dst,
        @NotNull OperationItem src,
        @NotNull TypeCode classType
    ) {
        // "%s = bitcast %s%s %s to %s*"
        return new Instruction(
            dst
            + " = bitcast "
            + src.getType()
            + "*".repeat(src.getPointerLevel())
            + " "
            + src
            + " to "
            + classType
            + "*"
        );
    }

    /**
     * Array definition
     * @param type Array type
     * @return Instruction
     */
    @NotNull
    public Instruction arrayDef(@NotNull TypeCode type) {
        assert type.isArray();
        Instruction i = new Instruction();
        i.add(comment("Array definition: " + type.getRealName()));
        // "%s = type { %s, %s* }"
        i.add(new Instruction(
            type
            + " = type { "
            + TypeCode.CInt
            + ", "
            + TypeCode.forArray(type.getBaseType(), type.getDimension() - 1)
            + "* }"
        ));
        return i;
    }

    /**
     * Array content allocation
     * @param dst Destination variable
     * @param tmp Temp variable
     * @param len Array length
     * @param item Item type
     * @return Instruction
     */
    @NotNull
    public Instruction arrayAlloc(
        @NotNull Variable dst,
        @NotNull Variable tmp,
        @NotNull OperationItem len,
        @NotNull TypeCode item
    ) {
        Instruction i = new Instruction();
        List<OperationItem> args = new ArrayList<>();
        args.add(len);
        args.add(new Literal(TypeCode.CInt, item.getSize()));
        i.add(call(tmp, "calloc", args));
        i.add(cast(dst, tmp, dst.getType()));
        return i;
    }

    /**
     * Array index access
     * @param dst Destination variable
     * @param src Array content
     * @param index Index
     * @return Instruction
     */
    @NotNull
    public Instruction loadIndex(
        @NotNull Variable dst,
        @NotNull Variable src,
        @NotNull OperationItem index
    ) {
        // "%s = getelementptr inbounds %s, %s* %s, i32 %s"
        return new Instruction(
            dst
            + " = getelementptr inbounds "
            + src.getType()
            + ", "
            + src.getType()
            + "* "
            + src
            + ", i32 "
            + index
        );
    }
}
