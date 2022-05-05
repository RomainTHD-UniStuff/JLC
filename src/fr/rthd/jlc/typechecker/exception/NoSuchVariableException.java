package fr.rthd.jlc.typechecker.exception;

/**
 * No such variable
 * @author RomainTHD
 */
public class NoSuchVariableException extends TypeException {
    public NoSuchVariableException(String varName) {
        super(String.format("No such variable `%s`", varName));
    }
}
