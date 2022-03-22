package fr.rthd.jlc.compiler;

import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;

public abstract class Compiler {
    public abstract String compile(Prog p, Env<?, FunType> env);
}
