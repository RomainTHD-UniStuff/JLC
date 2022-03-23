package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;

public enum ComparisonOperator {
    EQ("eq"),
    NE("ne"),
    GE("ge"),
    GT("gt"),
    LE("le"),
    LT("lt");

    private final String _operand;

    ComparisonOperator(String op) {
        this._operand = op;
    }

    public String getOperand(TypeCode type) {
        String prefix = "";

        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            if (this == LT || this == LE || this == GT || this == GE) {
                prefix = "s";
            }
        } else if (type == TypeCode.CDouble) {
            prefix = "o";
        }

        return prefix + this._operand;
    }
}
