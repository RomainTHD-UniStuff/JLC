package fr.rthd.jlc;

public enum TypeCode {
    CInt("i32", 0, 1),
    CDouble("double", 0.0, 2),
    CBool("i1", false, 1),
    CVoid("void", null, 0),
    CString("i8*", "", 0);

    private final String _typename;
    private final Object _defaultValue;
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
