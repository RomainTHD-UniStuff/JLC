package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid declared type
 * @author RomainTHD
 */
public class InvalidDeclaredTypeException extends TypeException {
    public InvalidDeclaredTypeException(@NotNull TypeCode type) {
        super(String.format(
            "Invalid declared type received, found `%s`",
            type.getRealName()
        ));
    }

    public InvalidDeclaredTypeException(
        @NotNull TypeCode type,
        @NotNull String varName
    ) {
        super(String.format(
            "Invalid declared type received, found `%s` for variable `%s`",
            type.getRealName(),
            varName
        ));
    }
}
