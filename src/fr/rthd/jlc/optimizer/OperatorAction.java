package fr.rthd.jlc.optimizer;

import javalette.Absyn.Expr;

/**
 * Interface for function pointers
 * @param <T> Literal input type
 */
interface OperatorAction<T> {
    Expr execute(T lvalue, T rvalue);
}
