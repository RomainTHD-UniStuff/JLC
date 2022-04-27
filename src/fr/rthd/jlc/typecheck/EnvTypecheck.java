package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

/**
 * Environment for typechecker
 * @author RomainTHD
 * @see Env
 */
class EnvTypecheck extends Env<TypeCode, FunType, ClassType> {
    /**
     * Current function type
     */
    private FunType _currentFunction = null;

    /**
     * The function does return or not
     */
    private boolean _doesReturn;

    public EnvTypecheck(Env<?, FunType, ClassType> parent) {
        super(parent);
    }

    public boolean doesReturn() {
        return _doesReturn;
    }

    public void setReturn(boolean doesReturn) {
        _doesReturn = doesReturn;
    }

    public FunType getCurrentFunction() {
        return _currentFunction;
    }

    public void setCurrentFunction(FunType currentFunction) {
        _currentFunction = currentFunction;
    }
}
