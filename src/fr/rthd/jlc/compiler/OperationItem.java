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
    public final TypeCode type;

    /**
     * Constructor
     * @param type Item type
     */
    public OperationItem(TypeCode type) {
        this.type = type;
    }

    /**
     * Should be overridden by subclasses
     * @return String representation of the item
     */
    public abstract String toString();
}
