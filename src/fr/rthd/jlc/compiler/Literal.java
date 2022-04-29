package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Literal
 * @author RomainTHD
 * @see OperationItem
 */
@NonNls
public class Literal extends OperationItem {
    /**
     * Value
     */
    @Nullable
    private final Object _value;

    /**
     * Constructor
     * @param type Literal type
     * @param value Value
     */
    public Literal(@NotNull TypeCode type, @Nullable Object value) {
        super(type);
        _value = value;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        if (_value == null) {
            return "null";
        } else {
            return _value.toString();
        }
    }

    /**
     * @return Literal value
     */
    @Contract(pure = true)
    @Nullable
    public Object getValue() {
        return _value;
    }
}
