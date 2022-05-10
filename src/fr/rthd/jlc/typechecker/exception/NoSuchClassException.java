package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.NotNull;

/**
 * The class does not exist
 * @author RomainTHD
 */
public class NoSuchClassException extends TypeException {
    public NoSuchClassException(@NotNull TypeCode t) {
        this(t.getRealName());
    }

    public NoSuchClassException(@NotNull String s) {
        super(String.format(
            "Class not found: `%s`",
            s
        ));
    }
}
