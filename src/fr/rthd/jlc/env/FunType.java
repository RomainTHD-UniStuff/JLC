package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

public class FunType {
    public final TypeCode retType;
    public final String name;
    public final List<FunArg> args;

    public FunType(TypeCode retVal, String name, FunArg... args) {
        this(retVal, name, Arrays.asList(args));
    }

    public FunType(TypeCode retVal, String name, List<FunArg> args) {
        this.retType = retVal;
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
}
