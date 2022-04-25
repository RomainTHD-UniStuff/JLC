package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;

/**
 * LLVM comparison operator. For example, "==" with integers would be translated
 * to "seq"
 * @author RomainTHD
 */
class ComparisonOperator {
    public static final ComparisonOperator EQ = new ComparisonOperator("eq");
    public static final ComparisonOperator NE = new ComparisonOperator("ne");
    public static final ComparisonOperator LT = new ComparisonOperator("lt");
    public static final ComparisonOperator LE = new ComparisonOperator("le");
    public static final ComparisonOperator GT = new ComparisonOperator("gt");
    public static final ComparisonOperator GE = new ComparisonOperator("ge");
    private final String _name;
    private ComparisonOperator(String name) {
        this._name = name;
    }

    /**
     * @param op Operator
     * @param type Value type
     * @return Operand
     */
    public static String getOperand(ComparisonOperator op, TypeCode type) {
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

        return prefix + op._name;
    }
}
