package fr.rthd.jlc.typecheck.exception;

public class InvalidExpressionTypeException extends TypeException {
    public InvalidExpressionTypeException(String actual, String expected) {
        super(String.format(
            "Invalid expression type `%s`, expected `%s`",
            actual,
            expected
        ));
    }
}
