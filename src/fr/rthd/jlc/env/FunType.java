package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

public class FunType {
    public final TypeCode retType;
    public final String name;
    public final List<FunArg> args;

    private boolean _isExternal;
    private boolean _isMain = false;
    private boolean _isPure = true;

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
        StringBuilder argsTypes = new StringBuilder();
        for (FunArg typeArg : args) {
            argsTypes.append(typeArg.name)
                     .append(":")
                     .append(typeArg.type)
                     .append(" ");
        }

        return this.retType.name() + " <- " + argsTypes;
    }

    public boolean isMain() {
        return _isMain;
    }

    public void setAsMain() {
        _isMain = true;
    }

    public boolean isPure() {
        return _isPure;
    }

    public FunType setPure(boolean isPure) {
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
