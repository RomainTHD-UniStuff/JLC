package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Class attribute
 * @author RomainTHD
 */
@NonNls
public class Attribute {
    /**
     * Attribute type
     */
    @NotNull
    private final TypeCode _type;

    /**
     * Attribute logical name, like `x` for `self.x`
     */
    @NotNull
    private final String _name;

    /**
     * Constructor
     * @param type Attribute type
     * @param name Attribute name
     */
    public Attribute(@NotNull TypeCode type, @NotNull String name) {
        _type = type;
        _name = name;
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return String.format(
            "%s %s",
            _type.getRealName(),
            _name
        );
    }

    /**
     * @return Attribute type
     */
    @Contract(pure = true)
    @NotNull
    public TypeCode getType() {
        return _type;
    }

    /**
     * @return Attribute name
     */
    @Contract(pure = true)
    @NotNull
    public String getName() {
        return _name;
    }
}
