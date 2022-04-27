package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

/**
 * Generic operation item, either a variable or a literal
 * @author RomainTHD
 */
public abstract class OperationItem {
    /**
     * Item type
     */
    private final TypeCode _type;

    /**
     * Constructor
     * @param type Item type
     */
    public OperationItem(TypeCode type) {
        _type = type;
    }

    /**
     * Should be overridden by subclasses
     * @return String representation of the item
     */
    public abstract String toString();

    public TypeCode getType() {
        return _type;
    }
}
