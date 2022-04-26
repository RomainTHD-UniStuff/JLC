package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.EQU;
import javalette.Absyn.GE;
import javalette.Absyn.GTH;
import javalette.Absyn.LE;
import javalette.Absyn.LTH;
import javalette.Absyn.NE;
import javalette.Absyn.RelOp;

import java.util.Arrays;

class RelOpVisitor implements RelOp.Visitor<String, Void> {
    private final TypeCode _left;
    private final TypeCode _right;

    public RelOpVisitor(TypeCode left, TypeCode right) {
        this._left = left;
        this._right = right;
    }

    private boolean bothTypes(TypeCode... expected) {
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
