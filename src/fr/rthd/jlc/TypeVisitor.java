package fr.rthd.jlc;

import javalette.Absyn.Bool;
import javalette.Absyn.Class;
import javalette.Absyn.Doub;
import javalette.Absyn.Int;
import javalette.Absyn.Type;
import javalette.Absyn.Void;
import org.jetbrains.annotations.NotNull;

import static fr.rthd.jlc.TypeCode.CBool;
import static fr.rthd.jlc.TypeCode.CDouble;
import static fr.rthd.jlc.TypeCode.CInt;
import static fr.rthd.jlc.TypeCode.CVoid;

/**
 * Type visitor to transform a Javalette type to a TypeCode
 * @author RomainTHD
 */
public class TypeVisitor implements Type.Visitor<TypeCode, Void> {
    /**
     * Javalette type from TypeCode
     * @param t TypeCode type
     * @return Javalette type
     * @throws IllegalArgumentException If type is not supported
     * @see TypeCode
     */
    public static Type fromTypecode(TypeCode t) throws IllegalArgumentException {
        if (CVoid.equals(t)) {
            return new Void();
        } else if (CBool.equals(t)) {
            return new Bool();
        } else if (CInt.equals(t)) {
            return new Int();
        } else if (CDouble.equals(t)) {
            return new Doub();
        } else if (t.isObject()) {
            return new Class(t.getRealName());
        }

        throw new IllegalArgumentException("Unknown typecode: " + t);
    }

    public TypeCode visit(Bool t, Void ignored) {
        return CBool;
    }

    public TypeCode visit(Int t, Void ignored) {
        return CInt;
    }

    public TypeCode visit(Doub t, Void ignored) {
        return CDouble;
    }

    public TypeCode visit(javalette.Absyn.Void t, Void ignored) {
        return CVoid;
    }

    public TypeCode visit(Class t, Void ignored) {
        return TypeCode.forClass(t.ident_);
    }
}
