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
    /**
     * @see EnvTypecheck#checkReturn()
     */
    private final int _optimizationLevel;

    /**
     * Constructor
     * @param optimizationLevel Optimization level
     * @see EnvTypecheck#checkReturn()
     */
    public TypeChecker(int optimizationLevel) {
        _optimizationLevel = optimizationLevel;
    }

    @NotNull
    @Override
    public Prog accept(
        @NotNull Prog p,
        @NotNull Env<?, FunType, ClassType<?>> parentEnv
    ) {
        EnvTypecheck env = new EnvTypecheck(
            _optimizationLevel == 0,
            parentEnv
        );
        p = p.accept(new ProgSignatureVisitor(), env);
        return p.accept(new ProgVisitor(), env);
    }
}
