package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a for statement is not valid
 * @author RomainTHD
 */
public class InvalidForTypeException extends TypeException {
    public InvalidForTypeException(
        @NotNull TypeCode varType,
        @NotNull TypeCode exprType
    ) {
        super(String.format(
            "Invalid types for for statement, expected `%s[]`, found `%s`",
            varType.getRealName(),
            exprType.getRealName()
        ));
    }
}
