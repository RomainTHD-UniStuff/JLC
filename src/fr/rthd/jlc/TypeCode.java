package fr.rthd.jlc;

public enum TypeCode {
    CInt("i32", 1),
    CDouble("double", 2),
    CBool("i1", 1),
    CVoid("void", 0),
    CString("i8*", 0);

    private final String _typename;
    private final int _size;

    TypeCode(String typename, int size) {
        this._typename = typename;
        this._size = size;
    }

    public String toString() {
        return this._typename;
    }

    public int getSize() {
        return _size;
    }
}
