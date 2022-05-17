package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.utils.Choice;
import javalette.Absyn.Blk;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;

public class FuncDefVisitor implements FuncDef.Visitor<FuncDef, EnvOptimizer> {
    @Override
    public FuncDef visit(FnDef f, EnvOptimizer env) {
        FunTypeOptimizer func = env.lookupFun(f.ident_);
        if (func == null) {
            ClassType<FunTypeOptimizer> c = env.getCurrentClass();
            assert c != null;
            func = c.getMethod(f.ident_, false);
            assert func != null;
        }

        for (FunArg arg : func.getArgs()) {
            if (!arg.getType().isPrimitive()) {
                // Some seemingly pure functions can mutate their arguments
                func.setPure(Choice.FALSE);
            }
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
