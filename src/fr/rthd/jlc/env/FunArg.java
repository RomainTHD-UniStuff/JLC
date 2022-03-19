package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

public class FunArg {
    public final TypeCode type;
    public final String name;

    public FunArg(TypeCode type, String name) {
        this.type = type;
        this.name = name;
    }
}
