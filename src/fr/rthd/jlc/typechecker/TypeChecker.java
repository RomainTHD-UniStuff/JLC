package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Type checker
 * @author RomainTHD
 */
@NonNls
public class TypeChecker implements Visitor {
    @NotNull
    @Override
    public Prog accept(
        @NotNull Prog p,
        @NotNull Env<?, FunType, ClassType<?>> parentEnv
    ) {
        EnvTypecheck env = new EnvTypecheck(parentEnv);
        p = p.accept(new ProgSignatureVisitor(), env);
        return p.accept(new ProgVisitor(), env);
    }
}
