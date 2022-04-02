package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

public class FunType {
    public final TypeCode retType;
    public final String name;
    public final boolean external;
    public final List<FunArg> args;

    public FunType(
        TypeCode retVal,
        String name,
        boolean external,
        FunArg... args
    ) {
        this(retVal, name, external, Arrays.asList(args));
    }

    public FunType(
        TypeCode retVal,
        String name,
        boolean external,
        List<FunArg> args
    ) {
        this.retType = retVal;
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
}
