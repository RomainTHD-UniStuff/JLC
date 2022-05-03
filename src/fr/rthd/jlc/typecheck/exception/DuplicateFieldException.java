package fr.rthd.jlc.typecheck.exception;

/**
 * Field declared twice in the same class
 * @author RomainTHD
 */
public class DuplicateFieldException extends TypeException {
    public DuplicateFieldException(
        String fieldName,
        String className,
        String fieldType
    ) {
        super(String.format(
            "%s `%s` declared twice in class `%s`",
            fieldType.substring(0, 1).toUpperCase() + fieldType.substring(1),
            fieldName,
            className
        ));
    }
}
