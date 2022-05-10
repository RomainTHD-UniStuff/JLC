package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.EQU;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.NE;
import javalette.Absyn.RelOp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Relation operator visitor
 * @author RomainTHD
 */
@Nls
class RelOpVisitor implements RelOp.Visitor<String, Void> {
    /**
     * Left operand type
     */
    @NotNull
    private final TypeCode _left;

    /**
     * Right operand type
     */
    @NotNull
    private final TypeCode _right;

    /**
     * Constructor
     * @param left left operand type
     * @param right right operand type
     */
    public RelOpVisitor(@NotNull TypeCode left, @NotNull TypeCode right) {
        _left = left;
        _right = right;
    }

    /**
     * Check if both operands are of the same type
     * @param expected Expected types
     * @return True if all operands are of the same type
     */
    @Contract(pure = true)
    private boolean bothTypes(@NotNull TypeCode... expected) {
        return _left == _right && Arrays.asList(expected).contains(_left);
    }

    /**
     * Lower than
     * @param p Lower than
     * @param ignored Visitor pattern artifact
     * @return Error message
     */
    @Contract(pure = true)
    @Override
    public String visit(LTH p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "lower than";
    }

    /**
     * Lower or equal
     * @param p Lower or equal
     * @param ignored Visitor pattern artifact
     * @return Error message
     */
    @Contract(pure = true)
    @Override
    public String visit(LE p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "lower or equal";
    }

    /**
     * Greater than
     * @param p Greater than
     * @param ignored Visitor pattern artifact
     * @return Error message
     */
    @Contract(pure = true)
    @Override
    public String visit(GTH p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "greater than";
    }

    /**
     * Greater or equal
     * @param p Greater or equal
     * @param ignored Visitor pattern artifact
     * @return Error message
     */
    @Contract(pure = true)
    @Override
    public String visit(GE p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "greater or equal";
    }

    /**
     * Equal
     * @param p Equal
     * @param ignored Visitor pattern artifact
     * @return Error message
     */
    @Contract(pure = true)
    @Override
    public String visit(EQU p, Void ignored) {
        return bothTypes(
            TypeCode.CInt,
            TypeCode.CDouble,
            TypeCode.CBool
        ) || (_left.isObject() && _left == _right)
               ? null
               : "equality";
    }

    /**
     * Not equal
     * @param p Not equal
     * @param ignored Visitor pattern artifact
     * @return Error message
     */
    @Contract(pure = true)
    @Override
    public String visit(NE p, Void ignored) {
        return bothTypes(
            TypeCode.CInt,
            TypeCode.CDouble,
            TypeCode.CBool
        ) || (_left.isObject() && _left == _right)
               ? null
               : "difference";
    }
}
