package fr.rthd.jlc.typecheck.exception;

public class InvalidConditionTypeException extends TypeException {
    public InvalidConditionTypeException(String conditionName, String type) {
        super(String.format(
            "Invalid %s condition, found type `%s`",
            conditionName,
            type
        ));
    }
}
