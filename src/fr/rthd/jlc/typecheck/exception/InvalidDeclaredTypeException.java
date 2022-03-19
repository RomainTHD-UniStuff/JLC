package fr.rthd.jlc.typecheck.exception;

public class InvalidDeclaredTypeException extends TypeException {
    public InvalidDeclaredTypeException(String type) {
        super(String.format(
            "Invalid declared type received, found `%s`",
            type
        ));
    }

    public InvalidDeclaredTypeException(String type, String varName) {
        super(String.format(
            "Invalid declared type received, found `%s` for variable `%s`",
            type,
            varName
        ));
    }
}
