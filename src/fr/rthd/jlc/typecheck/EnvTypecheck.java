package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Environment for typechecker
 * @author RomainTHD
 * @see Env
 */
class EnvTypecheck extends Env<TypeCode, FunType, ClassType> {
    /**
     * Current function type
     */
    @Nullable
    private FunType _currentFunction = null;

    /**
     * The function does return or not
     */
    private boolean _doesReturn;

    public EnvTypecheck(@NotNull Env<?, FunType, ClassType> parent) {
        super(parent);
    }

    @Contract(pure = true)
    public boolean doesReturn() {
        return _doesReturn;
    }

    public void setReturn(boolean doesReturn) {
        _doesReturn = doesReturn;
    }

    @Contract(pure = true)
    @Nullable
    public FunType getCurrentFunction() {
        return _currentFunction;
    }

    public void setCurrentFunction(@Nullable FunType currentFunction) {
        _currentFunction = currentFunction;
    }
}
