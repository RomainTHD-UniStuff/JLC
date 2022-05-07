package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * No such function
 * @author RomainTHD
 */
public class NoSuchFunctionException extends TypeException {
    public NoSuchFunctionException(@NotNull String varName) {
        super(String.format("No such function `%s`", varName));
    }
}
