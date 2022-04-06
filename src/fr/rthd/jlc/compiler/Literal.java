package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

/**
 * Literal
 * @author RomainTHD
 * @see OperationItem
 */
public class Literal extends OperationItem {
    /**
     * Value
     */
    public final Object value;

    /**
     * Constructor
     * @param type Literal type
     * @param value Value
     */
    public Literal(TypeCode type, Object value) {
        super(type);
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
