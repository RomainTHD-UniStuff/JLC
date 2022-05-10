package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Cyclic inheritance
 * @author RomainTHD
 */
public class CyclicInheritanceException extends TypeException {
    public CyclicInheritanceException(
        @NotNull String base,
        @NotNull String superclass
    ) {
        super(String.format(
            "Cyclic inheritance between `%s` and `%s`",
            base,
            superclass
        ));
    }
}
