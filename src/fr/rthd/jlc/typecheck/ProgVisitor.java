package fr.rthd.jlc.typecheck;

import javalette.Absyn.ListTopDef;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;
import org.jetbrains.annotations.NonNls;

/**
 * Program visitor
 * @author RomainTHD
 */
@NonNls
class ProgVisitor implements Prog.Visitor<Prog, EnvTypecheck> {
    /**
     * Program
     * @param p Program
     * @param env Environment
     * @return Program
     */
    @Override
    public Program visit(Program p, EnvTypecheck env) {
        ListTopDef topDef = new ListTopDef();

        for (TopDef def : p.listtopdef_) {
            topDef.add(def.accept(new TopDefVisitor(), env));
        }

        return new Program(topDef);
    }
}
