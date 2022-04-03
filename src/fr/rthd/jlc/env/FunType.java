package fr.rthd.jlc.env;

import fr.rthd.jlc.Choice;
import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

public class FunType {
    public final TypeCode retType;
    public final String name;
    public final List<FunArg> args;

    private boolean _isExternal;
    private boolean _isMain = false;
    private Choice _isPure = Choice.UNDEFINED;

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
            retType.toString(),
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
