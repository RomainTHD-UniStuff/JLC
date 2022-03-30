package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public class Variable extends OperationItem {
    public final String name;

    private boolean _isGlobal;
    private boolean _isPointer;
    private int _size;

    public Variable(TypeCode type, String name) {
        super(type);
        this.name = name;
        this._isGlobal = false;
        this._isPointer = true;
        this._size = 1;
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

    public void setPointerStatus(boolean isPointer) {
        this._isPointer = isPointer;
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
