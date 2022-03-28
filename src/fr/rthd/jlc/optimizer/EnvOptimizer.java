package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

class EnvOptimizer extends Env<AnnotatedExpr<?>, FunType> {
    public EnvOptimizer(Env<?, FunType> env) {
        super(env);
    }
}
