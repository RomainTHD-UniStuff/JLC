package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * No such variable
 * @author RomainTHD
 */
public class NoSuchVariableException extends TypeException {
    public NoSuchVariableException(@NotNull String varName) {
        super(String.format("No such variable `%s`", varName));
    }
}
