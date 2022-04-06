package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

/**
 * Invalid returned type
 * @author RomainTHD
 */
public class InvalidReturnedTypeException extends TypeException {
    public InvalidReturnedTypeException(TypeCode expected, TypeCode actual) {
        super(String.format(
            "Invalid return type, expected `%s`, found `%s`",
            expected,
            actual
        ));
    }

    public InvalidReturnedTypeException(
        String funcName,
        TypeCode expected,
        TypeCode actual
    ) {
        super(String.format(
            "Invalid return type in %s, expected `%s`, found `%s`",
            funcName,
            expected,
            actual
        ));
    }
}
