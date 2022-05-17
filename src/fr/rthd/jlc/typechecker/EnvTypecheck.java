package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Environment for typechecker
 * @author RomainTHD
 * @see Env
 */
@NonNls
class EnvTypecheck extends Env<TypeCode, FunType, ClassType<? extends FunType>> {
    /**
     * Current function type
     */
    @Nullable
    private FunType _currentFunction = null;

    /**
     * The function does return or not
     */
    private boolean _doesReturn;

    /**
     * Constructor
     * @param parent Parent environment
     */
    public EnvTypecheck(@NotNull Env<?, FunType, ClassType<?>> parent) {
        super(parent);
    }

    /**
     * @return Does the function return or not
     */
    @Contract(pure = true)
    public boolean doesReturn() {
        return _doesReturn;
    }

    /**
     * Set the function return
     * @param doesReturn Does the function return or not
     */
    public void setReturn(boolean doesReturn) {
        _doesReturn = doesReturn;
    }

    /**
     * @return Current function
     */
    @Contract(pure = true)
    @Nullable
    public FunType getCurrentFunction() {
        return _currentFunction;
    }

    /**
     * Set the current function
     * @param currentFunction Current function
     */
    public void setCurrentFunction(@Nullable FunType currentFunction) {
        _currentFunction = currentFunction;
    }
}
