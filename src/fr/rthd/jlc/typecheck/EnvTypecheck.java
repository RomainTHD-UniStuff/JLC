package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

class EnvTypecheck extends Env<TypeCode, FunType> {
    public TypeCode currentFunctionType = null;

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
