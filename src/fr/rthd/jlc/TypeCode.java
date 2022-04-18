package fr.rthd.jlc;

/**
 * Type code
 * @author RomainTHD
 */
public class TypeCode {
    public static final TypeCode CInt = TypeCode.fromPrimitive(
        "int",
        "i32",
        0,
        1
    );

    public static final TypeCode CDouble = TypeCode.fromPrimitive(
        "double",
        "double",
        0.0,
        2
    );

    public static final TypeCode CBool = TypeCode.fromPrimitive(
        "boolean",
        "i1",
        false,
        1
    );

    public static final TypeCode CVoid = TypeCode.fromPrimitive(
        "void",
        "void"
    );

    public static final TypeCode CString = TypeCode.fromPrimitive(
        "string",
        "i8*"
    );
    /**
     * Real name in source code
     */
    private final String _realName;

    /**
     * Assembly name
     */
    private final String _assemblyName;

    /**
     * Default value
     */
    private final Object _defaultValue;

    /**
     * Size
     */
    private final int _size;

    /**
     * Primitive type or not
     */
    private final boolean _isPrimitive;

    private final TypeCode _baseType;

    private TypeCode(
        String realName,
        String assemblyName,
        Object defaultValue,
        int size,
        boolean isPrimitive,
        TypeCode baseType
    ) {
        this._realName = realName;
        this._assemblyName = assemblyName;
        this._defaultValue = defaultValue;
        this._size = size;
        this._isPrimitive = isPrimitive;
        this._baseType = baseType;
    }

    private static TypeCode fromPrimitive(
        String realName,
        String assemblyName,
        Object defaultValue,
        int size
    ) {
        return new TypeCode(
            realName,
            assemblyName,
            defaultValue,
            size,
            true,
            null
        );
    }

    private static TypeCode fromPrimitive(
        String realName,
        String assemblyName
    ) {
        return fromPrimitive(
            realName,
            assemblyName,
            null,
            0
        );
    }

    public static TypeCode forClass(String typeName) {
        return new TypeCode(
            typeName,
            "(TBD)",
            null,
            0,
            false,
            null
        );
    }

    public static TypeCode forArray(TypeCode baseType) {
        String realName = baseType._realName + "[]";
        return new TypeCode(
            realName,
            "(TBD)",
            null,
            0,
            false,
            baseType
        );
    }

    @Override
    public String toString() {
        return this._assemblyName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj instanceof TypeCode) {
            TypeCode other = (TypeCode) obj;
            return this._realName.equals(other._realName);
        } else {
            return false;
        }
    }

    public int getSize() {
        return _size;
    }

    public boolean isPrimitive() {
        return _isPrimitive;
    }

    public TypeCode getBaseType() {
        return _baseType;
    }

    public String getRealName() {
        return _realName;
    }

    public String getAssemblyName() {
        return _assemblyName;
    }

    public boolean isArray() {
        return _baseType != null;
    }

    public boolean isClass() {
        return _baseType == null && !_isPrimitive;
    }

    public Object getDefaultValue() {
        return _defaultValue;
    }
}
