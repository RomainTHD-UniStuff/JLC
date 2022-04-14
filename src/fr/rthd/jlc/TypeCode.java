package fr.rthd.jlc;

/**
 * Type code
 * @author RomainTHD
 */
public class TypeCode {
    public static final TypeCode CInt = new TypeCode("i32", 0, 1);
    public static final TypeCode CDouble = new TypeCode("double", 0.0, 2);
    public static final TypeCode CBool = new TypeCode("i1", false, 1);
    public static final TypeCode CVoid = new TypeCode("void", null, 0);
    public static final TypeCode CString = new TypeCode("i8*", "", 0);

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

    public TypeCode(String typename, Object defaultValue, int size) {
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
