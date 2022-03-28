package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.ComparisonOperator;

class LLVMComparisonOperator {
    public static String getOperand(ComparisonOperator op, TypeCode type) {
        String prefix = "";

        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            if (op == ComparisonOperator.LT ||
                op == ComparisonOperator.LE ||
                op == ComparisonOperator.GT ||
                op == ComparisonOperator.GE) {
                prefix = "s";
            }
        } else if (type == TypeCode.CDouble) {
            prefix = "o";
        }

        switch (op) {
            case EQ:
                return prefix + "eq";

            case NE:
                return prefix + "ne";

            case LT:
                return prefix + "lt";

            case LE:
                return prefix + "le";

            case GT:
                return prefix + "gt";

            case GE:
                return prefix + "ge";

            default:
                throw new RuntimeException("Unknown comparison operator");
        }
    }
}
