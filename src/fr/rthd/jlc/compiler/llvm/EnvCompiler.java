package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Instruction;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Environment of the compiler
 * @author RomainTHD
 * @see Env
 * @see Variable
 */
public class EnvCompiler extends Env<Variable, FunType, ClassType> {
    /**
     * Indent character
     */
    @NotNull
    public static final String INDENT = "\t";

    /**
     * Separator for variable fields
     */
    public static final char SEP = '$';

    /**
     * Instruction builder
     */
    @NotNull
    public final InstructionBuilder instructionBuilder;

    /**
     * Instructions output
     */
    @NotNull
    private final List<String> _output;

    /**
     * Variable counter to avoid collisions, like ``` .temp$0 = ... .temp$1 =
     * ... ```
     */
    @NotNull
    private final LinkedList<Map<String, Integer>> _varCount;

    /**
     * Label counter
     * @see #_varCount
     */
    @NotNull
    private final LinkedList<Map<String, Integer>> _labelCount;

    /**
     * Depth access counter, to avoid collisions between blocks like ``` {
     * .temp$0 = ... } { .temp$0 = ... ; Different block, but a collision } ```
     */
    @NotNull
    private final Map<Integer, Integer> _depthAccessCount;

    /**
     * Hashing algorithm to store strings
     */
    @Nullable
    private final MessageDigest _hashAlgorithm;

    /**
     * Indent level
     */
    private int _indentLevel;

    /**
     * Constructor
     * @param env Parent environment
     */
    public EnvCompiler(
        @NotNull Env<?, FunType, ClassType> env,
        @NotNull InstructionBuilder builder
    ) {
        super(env);
        instructionBuilder = builder;
        _output = new ArrayList<>();
        _varCount = new LinkedList<>();
        _labelCount = new LinkedList<>();
        _depthAccessCount = new HashMap<>();
        _depthAccessCount.put(getScopeDepth(), 0);
        _indentLevel = 0;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception ignored) {
            // MD5 not supported, use native `hashCode` instead
        }
        _hashAlgorithm = md;
    }

    /**
     * Output the instructions to a single string
     * @return Assembly string
     */
    @Contract(pure = true)
    @NotNull
    public String toAssembly() {
        StringBuilder res = new StringBuilder();
        for (String inst : _output) {
            res.append(inst).append("\n");
        }

        return res.toString();
    }

    public void indent() {
        ++_indentLevel;
    }

    public void unindent() {
        --_indentLevel;
    }

    @Contract(pure = true)
    @NotNull
    private String getIndentString() {
        return INDENT.repeat(_indentLevel);
    }

    /**
     * Emit an instruction
     * @param inst Instruction to emit
     */
    public void emit(@NotNull Instruction inst) {
        for (String emitted : inst.emit()) {
            if (emitted.isEmpty()) {
                _output.add("");
            } else if (inst.isIndentable()) {
                _output.add(getIndentString() + emitted);
            } else {
                _output.add(emitted);
            }
        }
    }

    /**
     * Emit an instruction at the beginning of the file. Mainly used for global
     * string literals
     * @param inst Instruction to emit
     */
    public void emitAtBeginning(@NotNull Instruction inst) {
        for (String emitted : inst.emit()) {
            if (emitted.isEmpty()) {
                _output.add(0, "");
            } else {
                _output.add(0, emitted);
            }
        }
    }

    /**
     * Get a variable unique ID
     * @param name Variable name
     * @return Variable ID
     */
    @NotNull
    private String getVariableUID(@NotNull String name) {
        Map<String, Integer> scope = _varCount.peek();
        assert scope != null;
        int count = scope.getOrDefault(name, 0);
        scope.put(name, count + 1);
        return String.format(
            "stack_%d_%d%cscope_%d",
            getScopeDepth(),
            _depthAccessCount.get(getScopeDepth()),
            SEP,
            count
        );
    }

    /**
     * Create a temporary variable
     * @param type Variable type
     * @param ctx Context, like "if", "while", etc
     * @return Temporary variable
     */
    @NotNull
    public Variable createTempVar(@NotNull TypeCode type, @NotNull String ctx) {
        return createTempVar(type, ctx, false);
    }

    /**
     * Create a temporary variable that is a pointer
     * @param type Variable type
     * @param ctx Context, like "if", "while", etc. Only used here for `and`
     *     and `or`
     * @param isPointer Whether the variable is a pointer or not
     * @return Temporary variable
     */
    @NotNull
    public Variable createTempVar(
        @NotNull TypeCode type,
        @NotNull String ctx,
        boolean isPointer
    ) {
        return new Variable(type, String.format(
            ".temp%s%c%s%c%s",
            isPointer ? "_ptr" : "",
            SEP,
            ctx,
            SEP,
            getVariableUID(ctx)
        ), isPointer);
    }

    /**
     * Create a variable
     * @param type Variable type
     * @param name Variable name
     * @param isPointer Whether the variable is a pointer or not
     * @return Variable
     */
    @NotNull
    public Variable createVar(
        @NotNull TypeCode type,
        @NotNull String name,
        boolean isPointer
    ) {
        return createVar(type, name, isPointer, false);
    }

    /**
     * Create a variable
     * @param type Variable type
     * @param name Variable name
     * @param isPointer Whether the variable is a pointer or not
     * @param isClassVariable Whether the variable is a class variable or
     *     not
     * @return Variable
     */
    @NotNull
    public Variable createVar(
        @NotNull TypeCode type,
        @NotNull String name,
        boolean isPointer,
        boolean isClassVariable
    ) {
        return new Variable(type, String.format(
            "%s%c%s",
            name,
            SEP,
            getVariableUID(name)
        ), isPointer, isClassVariable);
    }

    /**
     * Create a global string literal
     * @param content String content
     * @return String literal address
     */
    @NotNull
    public Variable createGlobalStringLiteral(@NotNull String content) {
        return new Variable(TypeCode.CString, String.format(
            ".string%c%s",
            SEP,
            getHash(content)
        ), false, false, true, content.length() + 1);
    }

    /**
     * Create a new label
     * @param ctx Label context, like "if_true", "while_loop", etc
     * @return Label
     */
    @NotNull
    public String getNewLabel(@NotNull String ctx) {
        Map<String, Integer> scope = _labelCount.peek();
        assert scope != null;
        int count = scope.getOrDefault(ctx, 0);
        scope.put(ctx, count + 1);
        return String.format(
            ".label%c%s%cstack_%d_%d%cscope_%d",
            SEP,
            ctx,
            SEP,
            getScopeDepth(),
            _depthAccessCount.get(getScopeDepth()),
            SEP,
            count
        );
    }

    @Override
    public void enterScope() {
        super.enterScope();
        _varCount.push(new HashMap<>());
        _labelCount.push(new HashMap<>());
        int depth = getScopeDepth();
        _depthAccessCount.put(
            depth,
            _depthAccessCount.getOrDefault(depth, -1) + 1
        );
    }

    @Override
    public void leaveScope() {
        super.leaveScope();
        _varCount.pop();
        _labelCount.pop();
    }

    @Override
    public void resetScope() {
        super.resetScope();
        _varCount.clear();
        _varCount.push(new HashMap<>());
        _labelCount.clear();
        _labelCount.push(new HashMap<>());
    }

    /**
     * @param content String
     * @return Hash of the string
     */
    @NotNull
    private String getHash(@NotNull String content) {
        if (_hashAlgorithm == null) {
            return String.format("%x", content.hashCode());
        } else {
            _hashAlgorithm.update(content.getBytes());
            byte[] bytes = _hashAlgorithm.digest();
            BigInteger bi = new BigInteger(1, bytes);
            return String.format("%0" + (bytes.length << 1) + "X", bi);
        }
    }
}
