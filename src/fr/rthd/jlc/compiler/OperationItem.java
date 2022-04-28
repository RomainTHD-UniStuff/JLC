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
     * Pointer or not
     */
    private final boolean _isPointer;

    /**
     * Constructor
     * @param type Item type
     */
    public OperationItem(TypeCode type) {
        this(type, false);
    }

    /**
     * Constructor
     * @param type Item type
     * @param isPointer Pointer or not
     */
    public OperationItem(TypeCode type, boolean isPointer) {
        _type = type;
        _isPointer = isPointer;
    }

    /**
     * Should be overridden by subclasses
     * @return String representation of the item
     */
    public abstract String toString();

    public TypeCode getType() {
        return _type;
    }

    public boolean isPointer() {
        return _isPointer;
    }
}
