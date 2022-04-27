package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

/**
 * Variable
 * @author RomainTHD
 * @see OperationItem
 */
public class Variable extends OperationItem {
    /**
     * Variable name
     */
    public final String name;

    /**
     * Global or not
     */
    private boolean _isGlobal;

    /**
     * Pointer or not
     */
    private final boolean _isPointer;

    /**
     * Class variable or not
     */
    private final boolean _isClassVariable;

    /**
     * Variable size. Only used for strings for now
     */
    private int _size;

    /**
     * Constructor
     * @param type Variable type
     * @param name Variable name
     * @param isPointer Is a pointer or not
     * @param isClassVariable Is a class variable or not
     */
    public Variable(
        TypeCode type,
        String name,
        boolean isPointer,
        boolean isClassVariable
    ) {
        super(type);
        this.name = name;
        this._isGlobal = false;
        this._isPointer = isPointer;
        this._size = 1;
        this._isClassVariable = isClassVariable;
    }

    /**
     * Constructor
     * @param type Variable type
     * @param name Variable name
     * @param isPointer Is a pointer or not
     */
    public Variable(
        TypeCode type,
        String name,
        boolean isPointer
    ) {
        this(type, name, isPointer, false);
    }

    public boolean isGlobal() {
        return this._isGlobal;
    }

    public void setGlobal() {
        this._isGlobal = true;
    }

    public boolean isPointer() {
        return this._isPointer;
    }

    public boolean isClassVariable() {
        return this._isClassVariable;
    }

    public int getSize() {
        return this._size;
    }

    public void setSize(int size) {
        this._size = size;
    }

    @Override
    public String toString() {
        // Example: "%tmp"
        return String.format(
            "%c%s",
            this.isGlobal() ? '@' : '%',
            name
        );
    }
}
