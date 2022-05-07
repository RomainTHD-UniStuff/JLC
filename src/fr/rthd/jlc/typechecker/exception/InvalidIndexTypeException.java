package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid index type
 * @author RomainTHD
 */
public class InvalidIndexTypeException extends TypeException {
    public InvalidIndexTypeException(@NotNull TypeCode type, int idx) {
        super(String.format(
            "Invalid index type, `%s` found instead of `%s` at index %d",
            type.getRealName(),
            TypeCode.CInt.getRealName(),
            idx
        ));
    }
}
