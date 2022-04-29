package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.EQU;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.NE;
import javalette.Absyn.RelOp;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

class RelOpVisitor implements RelOp.Visitor<String, Void> {
    @NotNull
    private final TypeCode _left;

    @NotNull
    private final TypeCode _right;

    public RelOpVisitor(@NotNull TypeCode left, @NotNull TypeCode right) {
        _left = left;
        _right = right;
    }

    @Contract(pure = true)
    private boolean bothTypes(@NotNull TypeCode... expected) {
        return _left == _right && Arrays.asList(expected).contains(_left);
    }

    public String visit(LTH p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "lower than";
    }

    public String visit(LE p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "lower or equal";
    }

    public String visit(GTH p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "greater than";
    }

    public String visit(GE p, Void ignored) {
        return bothTypes(TypeCode.CInt, TypeCode.CDouble)
               ? null
               : "greater or equal";
    }

    public String visit(EQU p, Void ignored) {
        return bothTypes(
            TypeCode.CInt,
            TypeCode.CDouble,
            TypeCode.CBool
        ) || (_left.isObject() && _left == _right)
               ? null
               : "equality";
    }

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
