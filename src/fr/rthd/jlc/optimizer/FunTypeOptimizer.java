package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.env.FunType;

public class FunTypeOptimizer extends FunType {
    private boolean _isUsed;

    public FunTypeOptimizer(FunType funType, boolean isUsed) {
        super(funType);
        _isUsed = isUsed;
    }

    public void setAsUsed() {
        // FIXME: Doesn't work with recursive functions or circular calls
        this._isUsed = true;
    }

    public boolean isUsed() {
        return _isUsed;
    }
}
