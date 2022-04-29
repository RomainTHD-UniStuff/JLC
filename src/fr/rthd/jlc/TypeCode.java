package fr.rthd.jlc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Type code
 * @author RomainTHD
 */
@NonNls
public class TypeCode {
    @NotNull
    public static final TypeCode CInt = TypeCode.fromPrimitive(
        "int",
        "i32",
        0,
        1
    );

    @NotNull
    public static final TypeCode CDouble = TypeCode.fromPrimitive(
        "double",
        "double",
        0.0,
        2
    );

    @NotNull
    public static final TypeCode CBool = TypeCode.fromPrimitive(
        "boolean",
        "i1",
        false,
        1
    );

    @NotNull
    public static final TypeCode CVoid = TypeCode.fromPrimitive(
        "void",
        "void"
    );

    @NotNull
    public static final TypeCode CString = TypeCode.fromPrimitive(
        "string",
        "i8*"
    );

    /**
     * Type code pool, to avoid problems with class and array type comparison,
     * like `Dog == Dog` or `Dog[] == Dog[]`, because they might be different
     * instances.
     */
    @NotNull
    private static final Map<String, TypeCode> _pool = new HashMap<>();

    /**
     * Real name in source code
     */
    @NotNull
    private final String _realName;

    /**
     * Assembly name
     */
    @NotNull
    private final String _assemblyName;

    /**
     * Default value
     */
    @Nullable
    private final Object _defaultValue;

    /**
     * Size
     */
    private final int _size;

    /**
     * Primitive type or not
     */
    private final boolean _isPrimitive;

    /**
     * Base type for arrays
     */
    @Nullable
    private final TypeCode _baseType;

    private TypeCode(
        @NotNull String realName,
        @NotNull String assemblyName,
        @Nullable Object defaultValue,
        int size,
        boolean isPrimitive,
        @Nullable TypeCode baseType
    ) {
        _realName = realName;
        _assemblyName = assemblyName;
        _defaultValue = defaultValue;
        _size = size;
        _isPrimitive = isPrimitive;
        _baseType = baseType;
    }

    private static TypeCode fromPrimitive(
        @NotNull String realName,
        @NotNull String assemblyName,
        @Nullable Object defaultValue,
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
        @NotNull String realName,
        @NotNull String assemblyName
    ) {
        return fromPrimitive(
            realName,
            assemblyName,
            null,
            0
        );
    }

    @NotNull
    public static TypeCode forClass(@NotNull String realName) {
        TypeCode typeCode = _pool.get(realName);
        if (typeCode == null) {
            typeCode = new TypeCode(
                realName,
                "%" + realName,
                null,
                0,
                false,
                null
            );
            _pool.put(realName, typeCode);
        }

        return typeCode;
    }

    @NotNull
    public static TypeCode forArray(@NotNull TypeCode baseType) {
        String realName = baseType._realName + "[]";
        TypeCode typeCode = _pool.get(realName);
        if (typeCode == null) {
            typeCode = new TypeCode(
                realName,
                "(TBD)",
                null,
                0,
                false,
                baseType
            );
            _pool.put(realName, typeCode);
        }

        return typeCode;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return _assemblyName;
    }

    @Contract(pure = true)
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj instanceof TypeCode) {
            TypeCode other = (TypeCode) obj;
            return _realName.equals(other._realName);
        } else {
            return false;
        }
    }

    @Contract(pure = true)
    public int getSize() {
        return _size;
    }

    @Contract(pure = true)
    public boolean isPrimitive() {
        return _isPrimitive;
    }

    @Contract(pure = true)
    @Nullable
    public TypeCode getBaseType() {
        return _baseType;
    }

    @Contract(pure = true)
    @NotNull
    public String getRealName() {
        return _realName;
    }

    @Contract(pure = true)
    @NotNull
    public String getAssemblyName() {
        return _assemblyName;
    }

    @Contract(pure = true)
    public boolean isArray() {
        return _baseType != null;
    }

    @Contract(pure = true)
    public boolean isObject() {
        return _baseType == null && !_isPrimitive;
    }

    @Contract(pure = true)
    @Nullable
    public Object getDefaultValue() {
        return _defaultValue;
    }
}
