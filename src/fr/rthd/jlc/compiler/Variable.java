package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public class Variable extends OperationItem {
    public final String name;
    public final int address;

    public Variable(TypeCode type, String name, int address) {
        super(type);
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString() {
        // Example: "%tmp"
        return String.format("%%%s", name);
    }
}
