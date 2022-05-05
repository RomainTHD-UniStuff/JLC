package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;

/**
 * Invalid expression type
 * @author RomainTHD
 */
public class InvalidExpressionTypeException extends TypeException {
    public InvalidExpressionTypeException(TypeCode actual, TypeCode expected) {
        super(String.format(
            "Invalid expression type `%s`, expected `%s`",
            actual.getRealName(),
            expected.getRealName()
        ));
    }
}
