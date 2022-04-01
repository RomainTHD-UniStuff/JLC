package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

public class FunType {
    public final TypeCode retType;
    public final String name;
    public final boolean external;
    public final List<FunArg> args;

    private boolean _isMain;

    public FunType(FunType other) {
        this(other.retType, other.name, other.external, other.args);
        this._isMain = other._isMain;
    }

    public FunType(
        TypeCode retType,
        String name,
        boolean external,
        FunArg... args
    ) {
        this(retType, name, external, Arrays.asList(args));
    }

    public FunType(
        TypeCode retType,
        String name,
        boolean external,
        List<FunArg> args
    ) {
        this.retType = retType;
        this.name = name;
        this.external = external;
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
}
