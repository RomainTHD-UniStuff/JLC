package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typecheck.exception.NoReturnException;
import javalette.Absyn.Blk;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;

class FuncDefVisitor implements FuncDef.Visitor<FnDef, EnvTypecheck> {
    public FnDef visit(FnDef f, EnvTypecheck env) {
        FunType func = env.lookupFun(f.ident_);

        env.setReturn(false);
        env.enterScope();

        for (FunArg arg : func.args) {
            env.insertVar(arg.name, arg.type);
        }

        env.setCurrentFunction(func);

        Blk nBlock = f.blk_.accept(new BlkVisitor(), env);

        env.leaveScope();
        if (func.retType != TypeCode.CVoid && !env.doesReturn()) {
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
