package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public abstract class OperationItem {
    public final TypeCode type;

    public OperationItem(TypeCode type) {
        this.type = type;
    }

    public abstract String toString();
}
