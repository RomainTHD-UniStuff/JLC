package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.typechecker.exception.InvalidDeclaredTypeException;
import javalette.Absyn.Arg;
import javalette.Absyn.Argument;
import org.jetbrains.annotations.NonNls;

/**
 * Function argument visitor
 * @author RomainTHD
 */
@NonNls
class ArgVisitor implements Arg.Visitor<FunArg, Void> {
    /**
     * Function argument
     * @param a Function argument
     * @param ignored Visitor pattern artifact
     * @return Function argument
     */
    @Override
    public FunArg visit(Argument a, Void ignored) {
        TypeCode type = a.type_.accept(new TypeVisitor(), null);

        if (type == TypeCode.CVoid) {
            throw new InvalidDeclaredTypeException(type, a.ident_);
        }

        return new FunArg(type, a.ident_);
    }
}
