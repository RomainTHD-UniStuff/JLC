package fr.rthd.jlc.compiler;

import fr.rthd.jlc.env.FunType;

import java.util.List;

/**
 * Instruction builder, will generate all instructions
 * @author RomainTHD
 * @see Instruction
 * @see OperationItem
 */
public abstract class InstructionBuilder {
    /**
     * Output a comment
     * @param comment Comment to add
     * @return Instruction
     */
    public abstract Instruction comment(String comment);

    /**
     * No-op
     * @return Instruction
     */
    public abstract Instruction noop();

    /**
     * Store a value in a variable
     * @param dst Destination variable
     * @param src Value
     * @return Instruction
     */
    public abstract Instruction store(Variable dst, OperationItem src);

    /**
     * Load a variable in memory to a temporary variable
     * @param dst Destination variable
     * @param src Source variable
     * @return Instruction
     */
    public abstract Instruction load(Variable dst, Variable src);

    /**
     * Declare a new variable in memory
     * @param dst Destination variable
     * @return Instruction
     */
    public abstract Instruction declare(Variable dst);

    /**
     * Start a function declaration
     * @param func Function to declare
     * @return Instruction
     */
    public abstract Instruction functionDeclarationStart(FunType func);

    /**
     * End a function declaration
     * @return Instruction
     */
    public abstract Instruction functionDeclarationEnd();

    /**
     * Declare an external function
     * @param func External function to declare
     * @return Instruction
     */
    public abstract Instruction declareExternalFunction(FunType func);

    /**
     * Call a void function
     * @param funcName Function name
     * @param args Arguments
     * @return Instruction
     */
    public abstract Instruction call(
        String funcName,
        List<OperationItem> args
    );

    /**
     * Call a non-void function
     * @param dst Destination variable for return value
     * @param funcName Function name
     * @param args Arguments
     * @return Instruction
     */
    public abstract Instruction call(
        Variable dst,
        String funcName,
        List<OperationItem> args
    );

    /**
     * Output a label
     * @param labelName Label name
     * @return Instruction
     */
    public abstract Instruction label(String labelName);

    /**
     * Add two values
     * @param dst Destination variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction add(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * Increment a variable
     * @param dst Destination temp variable
     * @param src Input variable
     * @return Instruction
     */
    public abstract Instruction increment(
        Variable dst,
        Variable src
    );

    /**
     * Subtract two values
     * @param dst Destination variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction subtract(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * Decrement a variable
     * @param dst Destination temp variable
     * @param src Input variable
     * @return Instruction
     */
    public abstract Instruction decrement(
        Variable dst,
        Variable src
    );

    /**
     * Multiply two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction multiply(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * Divide two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction divide(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * Modulo two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction modulo(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * Compare two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param operator Operator
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction compare(
        Variable dst,
        OperationItem left,
        ComparisonOperator operator,
        OperationItem right
    );

    /**
     * Jump to a label
     * @param label Label name
     * @return Instruction
     */
    public abstract Instruction jump(String label);

    /**
     * Conditional jump, for if statements or while statements
     * @param condition Condition value, boolean
     * @param labelTrue Label name if condition is true
     * @param labelFalse Label name if condition is false
     * @return Instruction
     */
    public abstract Instruction conditionalJump(
        OperationItem condition,
        String labelTrue,
        String labelFalse
    );

    /**
     * Output a new line
     * @return Instruction
     */
    public Instruction newLine() {
        return new Instruction("");
    }

    /**
     * Void return
     * @return Instruction
     */
    public abstract Instruction ret();

    /**
     * Non-void return
     * @param returned Returned value
     * @return Instruction
     */
    public abstract Instruction ret(OperationItem returned);

    /**
     * AND two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction and(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * OR two values
     * @param dst Destination temp variable
     * @param left Left value
     * @param right Right value
     * @return Instruction
     */
    public abstract Instruction or(
        Variable dst,
        OperationItem left,
        OperationItem right
    );

    /**
     * Logic negation of a value
     * @param dst Destination temp variable
     * @param src Source value
     * @return Instruction
     */
    public abstract Instruction not(
        Variable dst,
        OperationItem src
    );

    /**
     * Mathematical negation of a value
     * @param dst Destination temp variable
     * @param src Source value
     * @return Instruction
     */
    public abstract Instruction neg(
        Variable dst,
        Variable src
    );

    /**
     * Global string literal
     * @param global Global variable
     * @param content String content
     * @return Instruction
     */
    public abstract Instruction globalStringLiteral(
        Variable global,
        String content
    );

    /**
     * Load a global string literal
     * @param dst Destination temp variable
     * @param global Global variable
     * @return Instruction
     */
    public abstract Instruction loadStringLiteral(
        Variable dst,
        Variable global
    );
}
