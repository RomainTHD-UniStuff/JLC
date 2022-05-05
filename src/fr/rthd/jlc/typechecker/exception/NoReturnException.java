package fr.rthd.jlc.typechecker.exception;

/**
 * No return
 * @author RomainTHD
 */
public class NoReturnException extends TypeException {
    public NoReturnException(String funcName) {
        super(String.format(
            "Function `%s` has no return statement, or not all paths return a value",
            funcName
        ));
    }
}
