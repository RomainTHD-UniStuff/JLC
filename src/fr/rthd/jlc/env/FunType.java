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

    public FunType(
        @NotNull TypeCode retType,
        @NotNull String name,
        @NotNull FunArg... args
    ) {
        _retType = retType;
        _name = name;
        _args = Arrays.asList(args);
    }

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

    @Contract(pure = true)
    public boolean isMain() {
        return _isMain;
    }

    public void setAsMain() {
        _isMain = true;
    }

    @Contract(pure = true)
    @NotNull
    public Choice isPure() {
        return _isPure;
    }

    @NotNull
    public FunType setPure(@NotNull Choice isPure) {
        _isPure = isPure;
        return this;
    }

    @Contract(pure = true)
    public boolean isExternal() {
        return _isExternal;
    }

    @NotNull
    public FunType setExternal() {
        _isExternal = true;
        return this;
    }

    public void addArgFirst(@NotNull FunArg arg) {
        _args.add(0, arg);
    }

    @Contract(pure = true)
    @NotNull
    public List<FunArg> getArgs() {
        return _args;
    }

    @Contract(pure = true)
    @NotNull
    public TypeCode getRetType() {
        return _retType;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return _name;
    }
}
