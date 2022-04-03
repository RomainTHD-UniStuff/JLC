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

    private int _pass = 0;

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

    public void newPass() {
        ++_pass;
    }

    public int getPassCount() {
        return _pass;
    }
}
