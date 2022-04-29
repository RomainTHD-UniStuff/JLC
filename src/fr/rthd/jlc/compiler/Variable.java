package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
        boolean isPointer
    ) {
        this(type, name, isPointer, false, false, 1);
    }

    /**
     * Constructor
     */
    public Variable(
        @NotNull TypeCode type,
        @NotNull String name,
        boolean isPointer,
        boolean isClassVariable
    ) {
        this(type, name, isPointer, isClassVariable, false, 1);
    }

    /**
     * Constructor
     */
    public Variable(
        @NotNull TypeCode type,
        @NotNull String name,
        boolean isPointer,
        boolean isClassAttribute,
        boolean isGlobal,
        int size
    ) {
        super(type, isPointer);
        _name = name;
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

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        // Globals start with a '@', locals with a '%'
        return String.format(
            "%c%s",
            isGlobal() ? '@' : '%',
            _name
        );
    }
}
