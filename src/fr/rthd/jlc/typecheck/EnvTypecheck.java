package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

/**
 * Environment for typechecker
 * @see Env
 * @author RomainTHD
 */
class EnvTypecheck extends Env<TypeCode, FunType> {
    /**
     * Current function type
     */
    public TypeCode currentFunctionType = null;

    /**
     * The function does return or not
     */
    private boolean _doesReturn;

    public EnvTypecheck(Env<?, FunType> parent) {
        super(parent);
    }

    public boolean doesReturn() {
        return this._doesReturn;
    }

    public void setReturn(boolean doesReturn) {
        this._doesReturn = doesReturn;
    }
}
