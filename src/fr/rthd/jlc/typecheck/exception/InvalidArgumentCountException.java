package fr.rthd.jlc.typecheck.exception;

public class InvalidArgumentCountException extends TypeException {
    public InvalidArgumentCountException(
        String funcName,
        int expected,
        int actual
    ) {
        super(String.format(
                  "Invalid number of arguments while calling the function `%s`, expected %d, found %d",
                  funcName,
                  expected,
                  actual
              )
        );
    }
}
