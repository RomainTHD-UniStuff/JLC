package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Function argument
 * @author RomainTHD
 */
@NonNls
public class FunArg {
    /**
     * Argument type
     */
    @NotNull
    private final TypeCode _type;

    /**
     * Argument logical name, like `x` for `f(x)`
     */
    @NotNull
    private final String _name;

    /**
     * Argument generated name, like `x$0$0` for `f(x)`
     */
    @Nullable
    private String _generatedName;

    /**
     * Constructor
     * @param type Argument type
     * @param name Argument name
     */
    public FunArg(@NotNull TypeCode type, @NotNull String name) {
        _type = type;
        _name = name;
        _generatedName = null;
    }

    @Contract(pure = true)
    @Nullable
    public String getGeneratedName() {
        return _generatedName;
    }

    public void setGeneratedName(@NotNull String name) {
        _generatedName = name;
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

    @Contract(pure = true)
    @NotNull
    public TypeCode getType() {
        return _type;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() {
        return _name;
    }
}
