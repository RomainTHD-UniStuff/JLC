package fr.rthd.jlc;

import javalette.Absyn.Bool;
import javalette.Absyn.Doub;
import javalette.Absyn.Int;
import javalette.Absyn.Type;

public class TypeVisitor implements Type.Visitor<TypeCode, Void> {
    public TypeCode visit(Bool t, Void ignored) {
        return TypeCode.CBool;
    }

    public TypeCode visit(Int t, Void ignored) {
        return TypeCode.CInt;
    }

    public TypeCode visit(Doub t, Void ignored) {
        return TypeCode.CDouble;
    }

    public TypeCode visit(javalette.Absyn.Void t, Void ignored) {
        return TypeCode.CVoid;
    }
}
