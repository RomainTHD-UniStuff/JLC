package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

/**
 * Exception thrown when a primitive is used on the `new` keyword
 * @author RomainTHD
 */
public class InvalidNewTypeException extends TypeException {
    public InvalidNewTypeException(TypeCode type) {
        super(String.format(
            "Cannot use primitive type `%s` with the `new` keyword",
            type.getRealName()
        ));
    }
}
