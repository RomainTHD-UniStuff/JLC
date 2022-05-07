package fr.rthd.jlc.typechecker.exception;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Invalid operation
 * @author RomainTHD
 */
public class InvalidOperationException extends TypeException {
    @NotNull
    private final String _message;

    public InvalidOperationException(
        @NotNull String operationName,
        @NotNull TypeCode actual,
        @NotNull TypeCode... expected
    ) {
        // Will be overridden later
        super("TBD");

        StringBuilder expectedTypes = new StringBuilder();
        if (expected.length == 1) {
            expectedTypes.append(String.format(
                "`%s`",
                expected[0].getRealName()
            ));
        } else {
            for (int i = 0; i < expected.length; ++i) {
                if (i == expected.length - 1) {
                    expectedTypes.append(String.format(
                        " or `%s`",
                        expected[i].getRealName()
                    ));
                } else {
                    expectedTypes.append(String.format(
                        "`%s`",
                        expected[i].getRealName()
                    ));
                    if (i != expected.length - 2) {
                        expectedTypes.append(", ");
                    }
                }
            }
        }

        _message = String.format(
            "Type mismatch in %s between `%s` and %s",
            operationName,
            actual.getRealName(),
            expectedTypes
        );
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String getMessage() {
        return _message;
    }
}
