package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid condition type
 * @author RomainTHD
 */
public class InvalidConditionTypeException extends TypeException {
    public InvalidConditionTypeException(
        @NotNull String conditionName,
        @NotNull TypeCode type
    ) {
        super(String.format(
            "Invalid %s condition, found type `%s`",
            conditionName,
            type.getRealName()
        ));
    }
}
