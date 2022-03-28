package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public class Variable extends OperationItem {
    public final String name;
    public final boolean isGlobal;
    public final int length;

    public Variable(TypeCode type, String name, boolean isGlobal, int length) {
        super(type);
        this.name = name;
        this.isGlobal = isGlobal;
        this.length = length;
    }

    public Variable(TypeCode type, String name) {
        this(type, name, false, 1);
    }

    @Override
    public String toString() {
        // Example: "%tmp"
        return String.format(
            "%c%s",
            isGlobal ? '@' : '%',
            name
        );
    }
}
