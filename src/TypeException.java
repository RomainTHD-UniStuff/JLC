class SymbolAlreadyDefinedException extends TypeException {
    public SymbolAlreadyDefinedException(String symbolName) {
        super(String.format("Symbol `%s` already defined", symbolName));
    }
}

class InvalidDeclaredTypeException extends TypeException {
    private final String message;

    public InvalidDeclaredTypeException(String type, String... names) {
        super("TBD");

        StringBuilder varNames = new StringBuilder();
        varNames.append("variable");
        if (names.length == 1) {
            varNames.append(String.format(" `%s`", names[0]));
        } else {
            for (int i = 0; i < names.length; ++i) {
                if (i == names.length - 1) {
                    varNames.append(String.format(" and `%s`", names[i]));
                } else {
                    varNames.append(String.format("`%s`", names[i]));
                    if (i != names.length - 2) {
                        varNames.append(", ");
                    }
                }
            }
        }

        this.message = String.format(
            "Invalid declared type received, found `%s` for %s",
            type,
            varNames
        );
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

class InvalidAssignmentTypeException extends TypeException {
    public InvalidAssignmentTypeException(
        String varName,
        String expected,
        String actual
    ) {
        super(String.format(
            "Invalid assignment to variable `%s` from type `%s` to type `%s`",
            varName,
            actual,
            expected
        ));
    }
}

class InvalidReturnedTypeException extends TypeException {
    public InvalidReturnedTypeException(String expected, String actual) {
        super(String.format(
            "Invalid return type, expected `%s`, found `%s`",
            expected,
            actual
        ));
    }
}

class InvalidConditionTypeException extends TypeException {
    public InvalidConditionTypeException(String conditionName, String type) {
        super(String.format(
            "Invalid %s condition, found type `%s`",
            conditionName,
            type
        ));
    }
}

class NoSuchVariableException extends TypeException {
    public NoSuchVariableException(String varName) {
        super(String.format("No such variable `%s`", varName));
    }
}

class NoSuchFunctionException extends TypeException {
    public NoSuchFunctionException(String varName) {
        super(String.format("No such function `%s`", varName));
    }
}

class InvalidArgumentCountException extends TypeException {
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

class InvalidOperationException extends TypeException {
    private final String message;

    public InvalidOperationException(
        String operationName,
        String actual,
        String... expected
    ) {
        super("TBD");

        StringBuilder expectedTypes = new StringBuilder();
        if (expected.length == 1) {
            expectedTypes.append(String.format("`%s`", expected[0]));
        } else {
            for (int i = 0; i < expected.length; ++i) {
                if (i == expected.length - 1) {
                    expectedTypes.append(String.format(
                        " or `%s`",
                        expected[i]
                    ));
                } else {
                    expectedTypes.append(String.format("`%s`", expected[i]));
                    if (i != expected.length - 2) {
                        expectedTypes.append(", ");
                    }
                }
            }
        }

        this.message = String.format(
            "Type mismatch in %s between `%s` and %s",
            operationName,
            actual,
            expectedTypes
        );
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}

public class TypeException extends RuntimeException {
    protected TypeException(String msg) {
        super(msg);
    }
}
