package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.AnnotatedLValue;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.internal.NotImplementedException;
import javalette.Absyn.LValue;
import javalette.Absyn.LValueP;
import javalette.Absyn.LValueV;

class LValueVisitor implements LValue.Visitor<AnnotatedLValue<?>, EnvCompiler> {
    @Override
    public AnnotatedLValue<?> visit(LValueV v, EnvCompiler env) {
        return new AnnotatedLValue<>(v.ident_, v);
    }

    @Override
    public AnnotatedLValue<?> visit(LValueP v, EnvCompiler env) {
        if (!(v.lvalue_ instanceof LValueV)) {
            throw new UnsupportedOperationException(
                "LValueP with non-LValueV lvalue"
            );
        }

        Variable var = env.lookupVar(v.ident_);
        assert var != null;

        LValueV right = (LValueV) v.lvalue_;
        if (var.getType().isArray()) {
            throw new NotImplementedException();
            /*
            if (right.ident_.equals("length")) {
                return new AnnotatedLValue<>(TypeCode.CInt, v);
            } else {
                throw new UnsupportedOperationException(
                    "LValueP with non-length identifier"
                );
            }*/
        } else if (var.getType().isObject()) {
            return new AnnotatedLValue<>(v.ident_, v, right.ident_);
        } else {
            throw new IllegalStateException("LValueP with non-object variable");
        }
    }
}
