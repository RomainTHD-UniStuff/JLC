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
    private final String _name;

    /**
     * Global or not
     */
    private final boolean _isGlobal;

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
    private final int _size;

    /**
     * Constructor
     */
    public Variable(
        TypeCode type,
        String name,
        boolean isPointer
    ) {
        this(type, name, isPointer, false, false, 1);
    }

    /**
     * Constructor
     */
    public Variable(
        TypeCode type,
        String name,
        boolean isPointer,
        boolean isClassVariable
    ) {
        this(type, name, isPointer, isClassVariable, false, 1);
    }

    /**
     * Constructor
     */
    public Variable(
        TypeCode type,
        String name,
        boolean isPointer,
        boolean isClassVariable,
        boolean isGlobal,
        int size
    ) {
        super(type);
        this._name = name;
        this._isPointer = isPointer;
        this._isClassVariable = isClassVariable;
        this._isGlobal = isGlobal;
        this._size = size;
    }

    public boolean isGlobal() {
        return this._isGlobal;
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

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        // Example: "%tmp"
        return String.format(
            "%c%s",
            this.isGlobal() ? '@' : '%',
            _name
        );
    }
}
