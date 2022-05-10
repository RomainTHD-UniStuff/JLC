package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid expression type
 * @author RomainTHD
 */
public class InvalidExpressionTypeException extends TypeException {
    public InvalidExpressionTypeException(
        @NotNull TypeCode actual,
        @NotNull TypeCode expected
    ) {
        super(String.format(
            "Invalid expression type `%s`, expected `%s`",
            actual.getRealName(),
            expected.getRealName()
        ));
    }
}
