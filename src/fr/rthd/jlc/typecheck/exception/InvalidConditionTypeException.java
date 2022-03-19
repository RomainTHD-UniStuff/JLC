package fr.rthd.jlc.typecheck.exception;

import fr.rthd.jlc.TypeCode;

public class InvalidConditionTypeException extends TypeException {
    public InvalidConditionTypeException(String conditionName, TypeCode type) {
        super(String.format(
            "Invalid %s condition, found type `%s`",
            conditionName,
            type
        ));
    }
}
