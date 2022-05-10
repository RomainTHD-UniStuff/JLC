package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typechecker.exception.NoReturnException;
import javalette.Absyn.Blk;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import org.jetbrains.annotations.NonNls;

/**
 * Function definition visitor
 * @author RomainTHD
 */
@NonNls
class FuncDefVisitor implements FuncDef.Visitor<FnDef, EnvTypecheck> {
    /**
     * Function definition
     * @param f Function definition
     * @param env Environment
     * @return Function definition
     */
    @Override
    public FnDef visit(FnDef f, EnvTypecheck env) {
        FunType func = env.lookupFun(f.ident_);
        assert func != null;

        env.setReturn(false);
        env.enterScope();

        for (FunArg arg : func.getArgs()) {
            env.insertVar(arg.getName(), arg.getType());
        }

        env.setCurrentFunction(func);

        Blk nBlock = f.blk_.accept(new BlkVisitor(), env);

        env.leaveScope();
        if (func.getRetType() != TypeCode.CVoid && !env.doesReturn()) {
            throw new NoReturnException(f.ident_);
        }

        return new FnDef(
            f.type_,
            f.ident_,
            f.listarg_,
            nBlock
        );
    }
}
