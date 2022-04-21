package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

/**
 * Invalid declared type
 * @author RomainTHD
 */
public class InvalidDeclaredTypeException extends TypeException {
    public InvalidDeclaredTypeException(TypeCode type) {
        super(String.format(
            "Invalid declared type received, found `%s`",
            type.getRealName()
        ));
    }

    public InvalidDeclaredTypeException(TypeCode type, String varName) {
        super(String.format(
            "Invalid declared type received, found `%s` for variable `%s`",
            type.getRealName(),
            varName
        ));
    }
}
