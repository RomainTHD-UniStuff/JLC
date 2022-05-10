package fr.rthd.jlc.typechecker.exception;

public class InvalidAssignmentException extends TypeException {
    public InvalidAssignmentException() {
        super(
            "Invalid assignment, expected a variable (lvalue), found a constant (rvalue)"
        );
    }
}
