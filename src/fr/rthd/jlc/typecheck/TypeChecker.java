package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;

/**
 * Type checker
 * @author RomainTHD
 */
public class TypeChecker implements Visitor {
    @Override
    public Prog accept(Prog p, Env<?, FunType, ClassType> parentEnv) {
        EnvTypecheck env = new EnvTypecheck(parentEnv);
        p = p.accept(new ProgSignatureVisitor(), env);
        return p.accept(new ProgVisitor(), env);
    }
}
