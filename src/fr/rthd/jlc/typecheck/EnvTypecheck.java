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

    private ClassType _currentClass = null;

    /**
     * The function does return or not
     */
    private boolean _doesReturn;

    public EnvTypecheck(Env<?, FunType, ClassType> parent) {
        super(parent);
    }

    public boolean doesReturn() {
        return this._doesReturn;
    }

    public void setReturn(boolean doesReturn) {
        this._doesReturn = doesReturn;
    }

    public FunType getCurrentFunction() {
        return _currentFunction;
    }

    public void setCurrentFunction(FunType currentFunction) {
        this._currentFunction = currentFunction;
    }

    public ClassType getCurrentClass() {
        return _currentClass;
    }

    public void setCurrentClass(ClassType currentClass) {
        this._currentClass = currentClass;
    }

    public void clearCurrentClass() {
        this._currentClass = null;
    }
}
