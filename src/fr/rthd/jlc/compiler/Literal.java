package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public class Literal extends OperationItem {
    public final Object value;

    public Literal(TypeCode type, Object value) {
        super(type);
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
