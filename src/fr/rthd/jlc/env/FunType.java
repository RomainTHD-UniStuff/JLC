package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.utils.Choice;

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
    private final TypeCode _retType;

    /**
     * Function name
     */
    private final String _name;

    /**
     * Arguments
     */
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
    private Choice _isPure = Choice.UNDEFINED;

    /**
     * Clone constructor
     * @param other Other function
     */
    public FunType(FunType other) {
        this(other._retType, other._name, other._args);
        _isExternal = other._isExternal;
        _isMain = other._isMain;
        _isPure = other._isPure;
    }

    public FunType(
        TypeCode retType,
        String name,
        FunArg... args
    ) {
        _retType = retType;
        _name = name;
        _args = Arrays.asList(args);
    }

    public FunType(
        TypeCode retType,
        String name,
        List<FunArg> args
    ) {
        _retType = retType;
        _name = name;
        _args = args;
    }

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

    public void addArgFirst(FunArg arg) {
        _args.add(0, arg);
    }

    public List<FunArg> getArgs() {
        return _args;
    }

    public TypeCode getRetType() {
        return _retType;
    }

    public String getName() {
        return _name;
    }
}
