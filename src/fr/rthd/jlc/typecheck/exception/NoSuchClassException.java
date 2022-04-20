package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

/**
 * The class does not exist
 * @author RomainTHD
 */
public class NoSuchClassException extends TypeException {
    public NoSuchClassException(TypeCode t) {
        this(t.getRealName());
    }

    public NoSuchClassException(String s) {
        super(String.format(
            "Class not found: `%s`",
            s
        ));
    }
}
