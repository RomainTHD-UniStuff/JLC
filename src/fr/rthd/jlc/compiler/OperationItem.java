package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Generic operation item, either a variable or a literal
 * @author RomainTHD
 */
@NonNls
public abstract class OperationItem {
    /**
     * Item type
     */
    @NotNull
    private final TypeCode _type;

    /**
     * Pointer or not
     */
    private final int _pointerLevel;

    /**
     * Constructor
     * @param type Item type
     */
    public OperationItem(@NotNull TypeCode type) {
        this(type, 0);
    }

    /**
     * Constructor
     * @param type Item type
     * @param pointerLevel Pointer level
     */
    public OperationItem(@NotNull TypeCode type, int pointerLevel) {
        _type = type;
        _pointerLevel = pointerLevel;
    }

    /**
     * Should be overridden by subclasses
     * @return String representation of the item
     */
    @Contract(pure = true)
    @NotNull
    @Override
    public abstract String toString();

    /**
     * @return Item type
     */
    @Contract(pure = true)
    @NotNull
    public TypeCode getType() {
        return _type;
    }

    /**
     * @return Pointer or not
     */
    @Contract(pure = true)
    public int getPointerLevel() {
        return _pointerLevel;
    }
}
