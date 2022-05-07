package fr.rthd.jlc.typechecker.exception;

/**
 * Function call to an expression that is not a function
 * @author RomainTHD
 */
public class InvalidFunctionCallException extends TypeException {
    public InvalidFunctionCallException() {
        super("Invalid function call, non-function expression found");
    }
}
