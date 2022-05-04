package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.internal.NotImplementedException;
import javalette.Absyn.Blk;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.LValueV;
import javalette.Absyn.ListIndex;
import javalette.Absyn.TopClsDef;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class TopDefVisitor implements TopDef.Visitor<TopDef, EnvOptimizer> {
    public TopFnDef visit(TopFnDef topF, EnvOptimizer env) {
        FnDef f = (FnDef) topF.funcdef_;
        FunTypeOptimizer func = env.lookupFun(f.ident_);
        assert func != null;
        env.setCurrentFunction(func);

        env.enterScope();

        for (FunArg arg : func.getArgs()) {
            env.insertVar(
                arg.getName(),
                new AnnotatedExpr<>(
                    arg.getType(),
                    new EVar(new LValueV(
                        arg.getName(),
                        new ListIndex()
                    ))
                )
            );
        }

        Blk nBlock = f.blk_.accept(new BlkVisitor(), env);

        env.leaveScope();

        return new TopFnDef(new FnDef(
            f.type_,
            f.ident_,
            f.listarg_,
            nBlock
        ));
    }

    public TopClsDef visit(TopClsDef p, EnvOptimizer env) {
        throw new NotImplementedException();
    }
}
