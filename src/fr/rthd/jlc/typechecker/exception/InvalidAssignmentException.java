package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Invalid assignment, either to an object attribute, to a method or to a
 * rvalue like `f() = 0`
 * @author RomainTHD
 */
public class InvalidAssignmentException extends TypeException {
    public InvalidAssignmentException(@NotNull String field) {
        super(String.format(
            "Invalid assignment to object field `%s`",
            field
        ));
    }

    public InvalidAssignmentException() {
        super("Invalid assignment to a rvalue");
    }
}
