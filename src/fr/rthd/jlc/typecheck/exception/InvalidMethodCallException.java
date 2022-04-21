package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

public class InvalidMethodCallException extends TypeException {
    public InvalidMethodCallException(TypeCode primitive) {
        super(String.format(
            "Invalid method call for primitive type `%s`",
            primitive
        ));
    }
}
