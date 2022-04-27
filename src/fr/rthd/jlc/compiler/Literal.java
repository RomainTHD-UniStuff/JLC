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
    private final Object _value;

    /**
     * Constructor
     * @param type Literal type
     * @param value Value
     */
    public Literal(TypeCode type, Object value) {
        super(type);
        this._value = value;
    }

    @Override
    public String toString() {
        if (_value == null) {
            return "null";
        } else {
            return _value.toString();
        }
    }

    public Object getValue() {
        return _value;
    }
}
