package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.AnnotatedLValue;
import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.internal.NotImplementedException;
import fr.rthd.jlc.typecheck.exception.InvalidMethodCallException;
import javalette.Absyn.EVar;
import javalette.Absyn.LValue;
import javalette.Absyn.LValueP;
import javalette.Absyn.LValueV;
import javalette.Absyn.ListIndex;

class LValueVisitor implements LValue.Visitor<AnnotatedLValue<?>, EnvTypecheck> {
    @Override
    public AnnotatedLValue<?> visit(LValueV v, EnvTypecheck env) {
        return new AnnotatedLValue<>(v.ident_, v);
    }

    @Override
    public AnnotatedLValue<?> visit(LValueP v, EnvTypecheck env) {
        if (!(v.lvalue_ instanceof LValueV)) {
            throw new UnsupportedOperationException(
                "LValueP with non-LValueV lvalue"
            );
        }

        new EVar(new LValueV(
            v.ident_,
            new ListIndex()
        )).accept(new ExprVisitor(), env);

        TypeCode varType = env.lookupVar(v.ident_);
        assert varType != null;
        // The type shouldn't be null because `EVar` already threw an exception
        //  if the variable wasn't found.

        LValueV right = (LValueV) v.lvalue_;
        if (varType.isArray()) {
            throw new NotImplementedException();
            /*
            if (right.ident_.equals("length")) {
                return new AnnotatedLValue<>(TypeCode.CInt, v);
            } else {
                throw new UnsupportedOperationException(
                    "LValueP with non-length identifier"
                );
            }*/
        } else if (varType.isObject()) {
            ClassType c = env.lookupClass(varType);
            if (c == null) {
                // It should be impossible to get here, since it would mean we
                //  created a variable with a type that doesn't exist
                throw new IllegalStateException("Class not found");
            }

            return new AnnotatedLValue<>(v.ident_, v, right.ident_);
        } else {
            throw new InvalidMethodCallException(varType);
        }
    }
}
