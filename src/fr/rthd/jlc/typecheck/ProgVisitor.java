package fr.rthd.jlc.typecheck;

import javalette.Absyn.ListTopDef;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;

class ProgVisitor implements Prog.Visitor<Prog, EnvTypecheck> {
    public Program visit(Program p, EnvTypecheck env) {
        ListTopDef topDef = new ListTopDef();

        for (TopDef def : p.listtopdef_) {
            topDef.add(def.accept(new TopDefVisitor(), env));
        }

        return new Program(topDef);
    }
}
