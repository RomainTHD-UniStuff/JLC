package fr.rthd.jlc.typecheck.exception;

public class InvalidAssignmentTypeException extends TypeException {
    public InvalidAssignmentTypeException(
        String varName,
        String expected,
        String actual,
        boolean isVar
    ) {
        super(String.format(
            "Invalid assignment to %s `%s` from type `%s` to type `%s`",
            isVar ? "variable" : "argument",
            varName,
            actual,
            expected
        ));
    }

    public InvalidAssignmentTypeException(
        String varName,
        String expected,
        String actual
    ) {
        this(varName, expected, actual, false);
    }
}
