package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.utils.Choice;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Function type
 * @author RomainTHD
 * @see FunArg
 */
@NonNls
public class FunType {
    /**
     * Return type
     */
    @NotNull
    private final TypeCode _retType;

    /**
     * Function name
     */
    @NotNull
    private final String _name;

    /**
     * Arguments
     */
    @NotNull
    private final List<FunArg> _args;

    /**
     * External or defined
     */
    private boolean _isExternal;

    /**
     * Main or not
     */
    private boolean _isMain = false;

    /**
     * Purity status
     * @see Choice
     */
    @NotNull
    private Choice _isPure = Choice.UNDEFINED;

    /**
     * Clone constructor
     * @param other Other function
     */
    public FunType(@NotNull FunType other) {
        this(other._retType, other._name, other._args);
        _isExternal = other._isExternal;
        _isMain = other._isMain;
        _isPure = other._isPure;
    }

    /**
     * Constructor
     * @param retType Return type
     * @param name Function name
     * @param args Arguments
     */
    public FunType(
        @NotNull TypeCode retType,
        @NotNull String name,
        @NotNull FunArg... args
    ) {
        _retType = retType;
        _name = name;
        _args = Arrays.asList(args);
    }

    /**
     * Constructor
     * @param retType Return type
     * @param name Function name
     * @param args Arguments
     */
    public FunType(
        @NotNull TypeCode retType,
        @NotNull String name,
        @NotNull List<FunArg> args
    ) {
        _retType = retType;
        _name = name;
        _args = args;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return String.format(
            "%s %s(%s)",
            _retType.getRealName(),
            _name,
            _args.stream()
                 .map(FunArg::toString)
                 .reduce((a, b) -> a + ", " + b)
                 .orElse("")
        );
    }

    /**
     * @return Is main function or not
     */
    @Contract(pure = true)
    public boolean isMain() {
        return _isMain;
    }

    /**
     * Set this function as main
     */
    public void setAsMain() {
        _isMain = true;
    }

    /**
     * @return Is pure or not
     */
    @Contract(pure = true)
    @NotNull
    public Choice isPure() {
        return _isPure;
    }

    /**
     * Set purity status
     * @param isPure Purity status
     * @return This
     */
    @NotNull
    public FunType setPure(@NotNull Choice isPure) {
        _isPure = isPure;
        return this;
    }

    /**
     * @return Is external or not
     */
    @Contract(pure = true)
    public boolean isExternal() {
        return _isExternal;
    }

    /**
     * Set this function as external
     * @return This
     */
    @NotNull
    public FunType setExternal() {
        _isExternal = true;
        return this;
    }

    /**
     * Add an argument to this function in first position
     * @param arg Argument to add
     */
    public void addArgFirst(@NotNull FunArg arg) {
        _args.add(0, arg);
    }

    /**
     * @return All the arguments
     */
    @Contract(pure = true)
    @NotNull
    public List<FunArg> getArgs() {
        return _args;
    }

    /**
     * @return Return type
     */
    @Contract(pure = true)
    @NotNull
    public TypeCode getRetType() {
        return _retType;
    }

    /**
     * @return Function name
     */
    @Contract(pure = true)
    @NotNull
    public String getName() {
        return _name;
    }
}
