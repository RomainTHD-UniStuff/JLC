package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Invalid argument count
 * @author RomainTHD
 */
public class InvalidArgumentCountException extends TypeException {
    public InvalidArgumentCountException(
        @NotNull String funcName,
        int expected,
        int actual
    ) {
        super(String.format(
                  "Invalid number of arguments while calling the function `%s`, " +
                  "expected %d, found %d",
                  funcName,
                  expected,
                  actual
              )
        );
    }
}
