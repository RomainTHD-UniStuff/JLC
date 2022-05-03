package fr.rthd.jlc.optimizer;

import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import javalette.Absyn.ListTopDef;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;
import javalette.Absyn.TopFnDef;

class ProgVisitor implements Prog.Visitor<Prog, EnvOptimizer> {
    public Program visit(Program p, EnvOptimizer env) {
        ListTopDef topDef = new ListTopDef();

        for (TopDef def : p.listtopdef_) {
            topDef.add(def.accept(new TopDefVisitor(), env));
        }

        ListTopDef usedTopDef = new ListTopDef();

        for (TopDef def : topDef) {
            if (def instanceof TopFnDef) {
                FuncDef fdef = ((TopFnDef) def).funcdef_;
                FunTypeOptimizer func = env.lookupFun(((FnDef) fdef).ident_);
                assert func != null;
                if (func.isUsedByMain()) {
                    func.updatePurity();
                    usedTopDef.add(def);
                } else {
                    env.removeFun(func.getName());
                }
            } else {
                // Class def, not implemented yet
                usedTopDef.add(def);
            }
        }

        for (TopDef def : usedTopDef) {
            if (def instanceof TopFnDef) {
                FuncDef fdef = ((TopFnDef) def).funcdef_;
                FunTypeOptimizer func = env.lookupFun(((FnDef) fdef).ident_);
                assert func != null;
                func.clearUsage();
            }
        }

        return new Program(usedTopDef);
    }
}
