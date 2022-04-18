package fr.rthd.jlc.env;

import fr.rthd.jlc.utils.Choice;
import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

/**
 * Function type
 * @author RomainTHD
 * @see FunArg
 */
public class FunType {
    /**
     * Return type
     */
    public final TypeCode retType;

    /**
     * Function name
     */
    public final String name;

    /**
     * Arguments
     */
    public final List<FunArg> args;

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
    private Choice _isPure = Choice.UNDEFINED;

    /**
     * Clone constructor
     * @param other
     */
    public FunType(FunType other) {
        this(other.retType, other.name, other.args);
        this._isExternal = other._isExternal;
        this._isMain = other._isMain;
        this._isPure = other._isPure;
    }

    public FunType(
        TypeCode retType,
        String name,
        FunArg... args
    ) {
        this.retType = retType;
        this.name = name;
        this.args = Arrays.asList(args);
    }

    public FunType(
        TypeCode retType,
        String name,
        List<FunArg> args
    ) {
        this.retType = retType;
        this.name = name;
        this.args = args;
    }

    @Override
    public String toString() {
        return String.format(
            "%s %s(%s)",
            retType.getRealName(),
            name,
            args.stream()
                .map(FunArg::toString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("")
        );
    }

    public boolean isMain() {
        return _isMain;
    }

    public void setAsMain() {
        _isMain = true;
    }

    public Choice isPure() {
        return _isPure;
    }

    public FunType setPure(Choice isPure) {
        _isPure = isPure;
        return this;
    }

    public boolean isExternal() {
        return _isExternal;
    }

    public FunType setExternal() {
        _isExternal = true;
        return this;
    }
}
