package fr.rthd.jlc;

import javalette.Absyn.Bool;
import javalette.Absyn.Class;
import javalette.Absyn.Doub;
import javalette.Absyn.Int;
import javalette.Absyn.Type;

/**
 * Type visitor to transform a Javalette type to a TypeCode
 * @author RomainTHD
 */
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

    /*
    public TypeCode visit(Array t, Void ignored) {
        return TypeCode.forArray(t.type_.accept(this, null));
    }*/

    public TypeCode visit(Class t, Void ignored) {
        return TypeCode.forClass(t.ident_);
    }
}
