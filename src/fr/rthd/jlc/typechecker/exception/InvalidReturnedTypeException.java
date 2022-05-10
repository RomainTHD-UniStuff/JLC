package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid returned type
 * @author RomainTHD
 */
public class InvalidReturnedTypeException extends TypeException {
    public InvalidReturnedTypeException(
        @NotNull TypeCode expected,
        @NotNull TypeCode actual
    ) {
        super(String.format(
            "Invalid return type, expected `%s`, found `%s`",
            expected.getRealName(),
            actual.getRealName()
        ));
    }

    public InvalidReturnedTypeException(
        @NotNull String funcName,
        @NotNull TypeCode expected,
        @NotNull TypeCode actual
    ) {
        super(String.format(
            "Invalid return type in %s, expected `%s`, found `%s`",
            funcName,
            expected.getRealName(),
            actual.getRealName()
        ));
    }
}
