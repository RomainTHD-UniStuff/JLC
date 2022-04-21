package fr.rthd.jlc;

import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;

public interface Visitor {
    /**
     * Entry point
     * @param p Program to visit
     * @param parentEnv Parent environment
     * @return Visited program
     */
    Prog accept(Prog p, Env<?, FunType, ClassType> parentEnv);
}
