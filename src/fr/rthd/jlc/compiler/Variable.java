package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public class Variable {
    public final TypeCode type;
    public final String name;
    public final int address;

    public Variable(TypeCode type, String name, int address) {
        this.type = type;
        this.name = name;
        this.address = address;
    }
}
