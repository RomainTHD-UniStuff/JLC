package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Field declared twice in the same class
 * @author RomainTHD
 */
public class DuplicateFieldException extends TypeException {
    public DuplicateFieldException(
        @NotNull String fieldName,
        @NotNull String className,
        @NotNull String fieldType
    ) {
        super(String.format(
            "%s `%s` declared twice in class `%s`",
            fieldType.substring(0, 1).toUpperCase() + fieldType.substring(1),
            fieldName,
            className
        ));
    }
}
