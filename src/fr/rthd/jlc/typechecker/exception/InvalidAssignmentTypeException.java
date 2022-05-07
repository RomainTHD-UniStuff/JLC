package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;

/**
 * Invalid assignment type
 * @author RomainTHD
 */
public class InvalidAssignmentTypeException extends TypeException {
    public InvalidAssignmentTypeException(
        TypeCode expected,
        TypeCode actual
    ) {
        super(String.format(
            "Invalid assignment to variable from type `%s` to type `%s`",
            actual.getRealName(),
            expected.getRealName()
        ));
    }

    public InvalidAssignmentTypeException(
        String varName,
        TypeCode expected,
        TypeCode actual
    ) {
        super(String.format(
            "Invalid assignment to argument `%s` from type `%s` to type `%s`",
            varName,
            actual.getRealName(),
            expected.getRealName()
        ));
    }
}
