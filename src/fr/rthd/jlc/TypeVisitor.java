package fr.rthd.jlc;

import javalette.Absyn.BaseType;
import javalette.Absyn.Bool;
import javalette.Absyn.Class;
import javalette.Absyn.DimenT;
import javalette.Absyn.Doub;
import javalette.Absyn.Int;
import javalette.Absyn.ListDim;
import javalette.Absyn.TType;
import javalette.Absyn.Type;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Type visitor to transform a Javalette type to a TypeCode
 * @author RomainTHD
 */
@NonNls
public class TypeVisitor implements Type.Visitor<TypeCode, Void>, BaseType.Visitor<TypeCode, Void> {
    @NotNull
    public static Type getTypeFromTypecode(@NotNull TypeCode typecode) {
        BaseType base;
        if (TypeCode.CBool.equals(typecode)) {
            base = new Bool();
        } else if (TypeCode.CInt.equals(typecode)) {
            base = new Int();
        } else if (TypeCode.CDouble.equals(typecode)) {
            base = new Doub();
        } else if (TypeCode.CVoid.equals(typecode)) {
            base = new javalette.Absyn.Void();
        } else if (typecode.isObject()) {
            base = new javalette.Absyn.Class(typecode.getRealName());
        } else {
            throw new IllegalArgumentException(
                "Unknown type code: " + typecode
            );
        }

        ListDim listdim = new ListDim();
        for (int i = 0; i < typecode.getDimension(); ++i) {
            listdim.add(new DimenT());
        }

        return new TType(base, listdim);
    }

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
        return TypeCode.CBool;
    }

    /**
     * Integer type
     * @param t Integer type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CInt
     * @see TypeCode#CInt
     */
    public TypeCode visit(Int t, Void ignored) {
        return TypeCode.CInt;
    }

    /**
     * Double type
     * @param t Double type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CDouble
     * @see TypeCode#CDouble
     */
    public TypeCode visit(Doub t, Void ignored) {
        return TypeCode.CDouble;
    }

    /**
     * Void type
     * @param t Void type
     * @param ignored Unused, visitor pattern artifact
     * @return TypeCode.CVoid
     * @see TypeCode#CVoid
     */
    public TypeCode visit(javalette.Absyn.Void t, Void ignored) {
        return TypeCode.CVoid;
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
