package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Environment for the optimizer
 * @author RomainTHD
 * @see Env
 * @see AnnotatedExpr
 * @see FunTypeOptimizer
 */
class EnvOptimizer extends Env<AnnotatedExpr<?>, FunTypeOptimizer, ClassTypeOptimizer> {
    /**
     * Current function
     */
    @Nullable
    private FunTypeOptimizer _currentFunction = null;

    /**
     * Number of passes
     */
    private int _pass = 0;

    /**
     * Constant propagation status
     */
    private boolean _constantPropagationEnabled = true;

    /**
     * Constructor
     * @param env Parent environment
     */
    public EnvOptimizer(@NotNull Env<?, FunType, ClassType<?>> env) {
        super();
        for (FunType funType : env.getAllFun()) {
            // We receive `FunType` objects but need to store `FunTypeOptimizer`
            //  objects
            insertFun(new FunTypeOptimizer(funType));
        }
        for (ClassType<?> classType : env.getAllClass()) {
            insertClass(new ClassTypeOptimizer(classType));
        }
        for (ClassTypeOptimizer classType : getAllClass()) {
            classType.updateSuperclass(lookupClass(classType.getSuperclassName()));
        }
    }

    /**
     * @return Current function
     */
    @Contract(pure = true)
    @Nullable
    public FunTypeOptimizer getCurrentFunction() {
        return _currentFunction;
    }

    /**
     * Set the current function
     * @param funType Current function
     */
    public void setCurrentFunction(@Nullable FunTypeOptimizer funType) {
        _currentFunction = funType;
    }

    /**
     * Increment the pass count
     */
    @Contract(pure = true)
    public void newPass() {
        ++_pass;
    }

    /**
     * @return Pass count
     */
    @Contract(pure = true)
    public int getPassCount() {
        return _pass;
    }

    /**
     * Enable or disable constant propagation, for while conditions
     * @param enabled Enable or disable
     */
    public void setConstantPropagation(boolean enabled) {
        _constantPropagationEnabled = enabled;
    }

    /**
     * @return Constant propagation status
     */
    public boolean constantPropagationEnabled() {
        return _constantPropagationEnabled;
    }
}
