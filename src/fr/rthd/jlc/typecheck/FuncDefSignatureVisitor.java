package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.TypeVisitor;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Arg;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import org.jetbrains.annotations.NonNls;

import java.util.LinkedList;
import java.util.List;

/**
 * Function definition signature visitor
 * @author RomainTHD
 */
@NonNls
class FuncDefSignatureVisitor implements FuncDef.Visitor<Void, EnvTypecheck> {
    /**
     * Function definition
     * @param p Function definition
     * @param env Environment
     */
    @Override
    public Void visit(FnDef p, EnvTypecheck env) {
        List<FunArg> argsType = new LinkedList<>();
        for (Arg arg : p.listarg_) {
            argsType.add(arg.accept(new ArgVisitor(), null));
        }

        TypeCode retType = p.type_.accept(new TypeVisitor(), null);
        env.insertFun(new FunType(retType, p.ident_, argsType));

        return null;
    }
}
