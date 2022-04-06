package fr.rthd.jlc;

/**
 * Type code
 * @author RomainTHD
 */
public enum TypeCode {
    CInt("i32", 0, 1),
    CDouble("double", 0.0, 2),
    CBool("i1", false, 1),
    CVoid("void", null, 0),
    CString("i8*", "", 0);

    /**
     * Type name
     */
    private final String _typename;

    /**
     * Default value
     */
    private final Object _defaultValue;

    /**
     * Size
     */
    private final int _size;

    TypeCode(String typename, Object defaultValue, int size) {
        this._typename = typename;
        this._defaultValue = defaultValue;
        this._size = size;
    }

    public String toString() {
        return this._typename;
    }

    public int getSize() {
        return _size;
    }

    public Object getDefaultValue() {
        return _defaultValue;
    }
}
