package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.ComparisonOperator;

/**
 * LLVM comparison operator. For example, "==" with integers would be translated
 * to "seq".
 * @author RomainTHD
 * @see ComparisonOperator
 */
class LLVMComparisonOperator {
    /**
     * @param op Operator
     * @param type Value type
     * @return Operand
     * @throws IllegalArgumentException If the operator is not supported
     */
    public static String getOperand(
        ComparisonOperator op,
        TypeCode type
    ) throws IllegalArgumentException {
        String prefix = "";

        if (type == TypeCode.CInt || type == TypeCode.CBool) {
            if (op == ComparisonOperator.LT ||
                op == ComparisonOperator.LE ||
                op == ComparisonOperator.GT ||
                op == ComparisonOperator.GE) {
                // Signed operation
                prefix = "s";
            }
        } else if (type == TypeCode.CDouble) {
            // Floating point operation, ignore NaN
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
                throw new IllegalArgumentException("Unknown comparison operator");
        }
    }
}
