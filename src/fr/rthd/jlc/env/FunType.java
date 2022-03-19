package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

import java.util.Arrays;
import java.util.List;

public class FunType {
    public final TypeCode retType;
    public final List<FunArg> args;

    public FunType(TypeCode retVal, FunArg... args) {
        this(retVal, Arrays.asList(args));
    }

    public FunType(TypeCode retVal, List<FunArg> args) {
        this.retType = retVal;
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
