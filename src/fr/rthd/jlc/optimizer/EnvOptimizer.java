package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

/**
 * Environment for the optimizer
 */
class EnvOptimizer extends Env<AnnotatedExpr<?>, FunTypeOptimizer> {
    /**
     * Current function
     */
    private FunTypeOptimizer _currentFunction;

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
    public EnvOptimizer(Env<?, FunType> env) {
        super();
        for (FunType funType : env.getAllFun()) {
            // We receive `FunType` objects but need to store `FunTypeOptimizer`
            //  objects
            this.insertFun(new FunTypeOptimizer(funType));
        }
    }

    /**
     * @return Current function
     */
    public FunTypeOptimizer getCurrentFunction() {
        return _currentFunction;
    }

    /**
     * Set the current function
     * @param funType Current function
     */
    public void setCurrentFunction(FunTypeOptimizer funType) {
        _currentFunction = funType;
    }

    /**
     * Increment the pass count
     */
    public void newPass() {
        ++_pass;
    }

    /**
     * @return Pass count
     */
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
