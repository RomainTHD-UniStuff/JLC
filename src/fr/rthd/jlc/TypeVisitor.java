package fr.rthd.jlc;

import javalette.Absyn.BaseType;
import javalette.Absyn.Bool;
import javalette.Absyn.Class;
import javalette.Absyn.Doub;
import javalette.Absyn.Int;
import javalette.Absyn.TType;
import javalette.Absyn.Type;
import org.jetbrains.annotations.NonNls;

import static fr.rthd.jlc.TypeCode.CBool;
import static fr.rthd.jlc.TypeCode.CDouble;
import static fr.rthd.jlc.TypeCode.CInt;
import static fr.rthd.jlc.TypeCode.CVoid;

/**
 * Type visitor to transform a Javalette type to a TypeCode
 * @author RomainTHD
 */
@NonNls
public class TypeVisitor implements Type.Visitor<TypeCode, Void>, BaseType.Visitor<TypeCode, Void> {
    @Override
    public TypeCode visit(TType p, Void ignored) {
        TypeCode base = p.basetype_.accept(new TypeVisitor(), null);
        if (p.listdim_.size() == 0) {
            return base;
        } else {
            for (int i = 0; i < p.listdim_.size(); ++i) {
                // Populate all intermediate types, used for compiler
                TypeCode.forArray(base, i);
            }

            return TypeCode.forArray(base, p.listdim_.size());
        }
    }

    /**
     * Boolean type
     * @param t Boolean type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CBool
     * @see TypeCode#CBool
     */
    public TypeCode visit(Bool t, Void ignored) {
        return CBool;
    }

    /**
     * Integer type
     * @param t Integer type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CInt
     * @see TypeCode#CInt
     */
    public TypeCode visit(Int t, Void ignored) {
        return CInt;
    }

    /**
     * Double type
     * @param t Double type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CDouble
     * @see TypeCode#CDouble
     */
    public TypeCode visit(Doub t, Void ignored) {
        return CDouble;
    }

    /**
     * Void type
     * @param t Void type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CVoid
     * @see TypeCode#CVoid
     */
    public TypeCode visit(javalette.Absyn.Void t, Void ignored) {
        return CVoid;
    }

    /**
     * Object type
     * @param t Object type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CObject
     * @see TypeCode#forClass(String)
     */
    public TypeCode visit(Class t, Void ignored) {
        return TypeCode.forClass(t.ident_);
    }
}
