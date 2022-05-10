package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Variable
 * @author RomainTHD
 * @see OperationItem
 */
public class Variable extends OperationItem {
    /**
     * Variable name
     */
    @NotNull
    private final String _name;

    /**
     * Variable name in source code
     */
    @NotNull
    private final String _sourceName;

    /**
     * Global or not
     */
    private final boolean _isGlobal;

    /**
     * Class variable or not
     */
    private final boolean _isClassAttribute;

    /**
     * Variable size. Only used for strings for now
     */
    private final int _size;

    /**
     * Constructor
     */
    public Variable(
        @NotNull TypeCode type,
        @NotNull String name,
        @Nullable String sourceName,
        int pointerLevel
    ) {
        this(type, name, sourceName, pointerLevel, false, false, 1);
    }

    /**
     * Constructor
     */
    public Variable(
        @NotNull TypeCode type,
        @NotNull String name,
        @Nullable String sourceName,
        int pointerLevel,
        boolean isClassVariable
    ) {
        this(type, name, sourceName, pointerLevel, isClassVariable, false, 1);
    }

    /**
     * Constructor
     */
    public Variable(
        @NotNull TypeCode type,
        @NotNull String name,
        @Nullable String sourceName,
        int pointerLevel,
        boolean isClassAttribute,
        boolean isGlobal,
        int size
    ) {
        super(type, pointerLevel);
        _name = name;
        _sourceName = sourceName == null ? name : sourceName;
        _isClassAttribute = isClassAttribute;
        _isGlobal = isGlobal;
        _size = size;
    }

    /**
     * @return Is global variable or not
     */
    @Contract(pure = true)
    public boolean isGlobal() {
        return _isGlobal;
    }

    /**
     * @return Is class attribute or not
     */
    @Contract(pure = true)
    public boolean isClassAttribute() {
        return _isClassAttribute;
    }

    /**
     * @return Variable size
     */
    @Contract(pure = true)
    public int getSize() {
        return _size;
    }

    /**
     * @return Variable name
     */
    @Contract(pure = true)
    @NotNull
    public String getName() {
        return _name;
    }

    /**
     * @return Variable name in source code
     */
    @Contract(pure = true)
    @NotNull
    public String getSourceName() {
        return _sourceName;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        // Globals start with a '@', locals with a '%'
        return (isGlobal() ? "@" : "%") + _name;
    }
}
