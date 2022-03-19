package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

public class InvalidExpressionTypeException extends TypeException {
    public InvalidExpressionTypeException(TypeCode actual, TypeCode expected) {
        super(String.format(
            "Invalid expression type `%s`, expected `%s`",
            actual,
            expected
        ));
    }
}
