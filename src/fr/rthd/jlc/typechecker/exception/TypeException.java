package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Type exception
 * @author RomainTHD
 */
@Nls
public class TypeException extends RuntimeException {
    protected TypeException(@NotNull String msg) {
        super(msg);
    }
}
