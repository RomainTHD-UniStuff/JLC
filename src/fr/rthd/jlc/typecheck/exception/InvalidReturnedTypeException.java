package fr.rthd.jlc.typecheck.exception;

public class InvalidReturnedTypeException extends TypeException {
    public InvalidReturnedTypeException(String expected, String actual) {
        super(String.format(
            "Invalid return type, expected `%s`, found `%s`",
            expected,
            actual
        ));
    }

    public InvalidReturnedTypeException(String funcName, String expected, String actual) {
        super(String.format(
            "Invalid return type in %s, expected `%s`, found `%s`",
            funcName,
            expected,
            actual
        ));
    }
}
