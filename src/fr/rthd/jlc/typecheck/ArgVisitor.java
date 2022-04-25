package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.typecheck.exception.InvalidDeclaredTypeException;
import javalette.Absyn.Arg;
import javalette.Absyn.Argument;

class ArgVisitor implements Arg.Visitor<FunArg, Void> {
    public FunArg visit(Argument a, Void ignored) {
        TypeCode type = a.type_.accept(new TypeVisitor(), null);

        if (type == TypeCode.CVoid) {
            throw new InvalidDeclaredTypeException(type, a.ident_);
        }

        return new FunArg(type, a.ident_);
    }
}
