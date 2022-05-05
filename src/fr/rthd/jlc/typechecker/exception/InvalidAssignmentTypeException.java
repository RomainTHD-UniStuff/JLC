package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;

/**
 * Invalid assignment type
 * @author RomainTHD
 */
public class InvalidAssignmentTypeException extends TypeException {
    public InvalidAssignmentTypeException(
        String varName,
        TypeCode expected,
        TypeCode actual,
        boolean isVar
    ) {
        super(String.format(
            "Invalid assignment to %s `%s` from type `%s` to type `%s`",
            isVar ? "variable" : "argument",
            varName,
            actual.getRealName(),
            expected.getRealName()
        ));
    }

    public InvalidAssignmentTypeException(
        String varName,
        TypeCode expected,
        TypeCode actual
    ) {
        this(varName, expected, actual, false);
    }
}
