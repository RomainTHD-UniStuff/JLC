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
    private final boolean _isPointer;

    /**
     * Constructor
     * @param type Item type
     */
    public OperationItem(@NotNull TypeCode type) {
        this(type, false);
    }

    /**
     * Constructor
     * @param type Item type
     * @param isPointer Pointer or not
     */
    public OperationItem(@NotNull TypeCode type, boolean isPointer) {
        _type = type;
        _isPointer = isPointer;
    }

    /**
     * Should be overridden by subclasses
     * @return String representation of the item
     */
    @Contract(pure = true)
    @NotNull
    public abstract String toString();

    @Contract(pure = true)
    @NotNull
    public TypeCode getType() {
        return _type;
    }

    @Contract(pure = true)
    public boolean isPointer() {
        return _isPointer;
    }
}
