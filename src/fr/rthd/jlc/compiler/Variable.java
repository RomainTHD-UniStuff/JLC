package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public class Variable extends OperationItem {
    public final String name;

    public Variable(TypeCode type, String name) {
        super(type);
        this.name = name;
    }

    @Override
    public String toString() {
        // Example: "%tmp"
        return String.format("%%%s", name);
    }
}
