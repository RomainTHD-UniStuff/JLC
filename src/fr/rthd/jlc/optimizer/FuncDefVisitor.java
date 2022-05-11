package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Blk;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;

public class FuncDefVisitor implements FuncDef.Visitor<FuncDef, EnvOptimizer> {
    @Override
    public FuncDef visit(FnDef f, EnvOptimizer env) {
        FunTypeOptimizer func = env.lookupFun(f.ident_);
        if (func == null) {
            ClassType c = env.getCurrentClass();
            assert c != null;
            FunType fun = c.getMethod(f.ident_, false);
            assert fun != null;
            func = new FunTypeOptimizer(fun);
        }

        env.setCurrentFunction(func);
        env.enterScope();

        for (FunArg arg : func.getArgs()) {
            env.insertVar(
                arg.getName(),
                new AnnotatedExpr<>(
                    arg.getType(),
                    new EVar(arg.getName())
                )
            );
        }

        Blk nBlock = f.blk_.accept(new BlkVisitor(), env);

        env.leaveScope();

        return new FnDef(
            f.type_,
            f.ident_,
            f.listarg_,
            nBlock
        );
    }
}
