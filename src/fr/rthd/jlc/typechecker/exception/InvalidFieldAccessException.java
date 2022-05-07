package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid field access, like `0.length`
 * @author RomainTHD
 */
public class InvalidFieldAccessException extends TypeException {
    public InvalidFieldAccessException(
        @NotNull String field,
        @NotNull TypeCode type
    ) {
        super(String.format(
            "Invalid field access `%s` for type `%s`",
            field,
            type.getRealName()
        ));
    }
}
