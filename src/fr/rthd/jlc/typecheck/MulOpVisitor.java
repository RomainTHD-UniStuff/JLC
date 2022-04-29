package fr.rthd.jlc.typecheck;

import javalette.Absyn.Div;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.Times;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;

/**
 * Multiplication-like operator visitor
 * @author RomainTHD
 */
@Nls
class MulOpVisitor implements MulOp.Visitor<String, Void> {
    /**
     * Multiplication
     * @param p Multiplication
     * @param ignored Visitor pattern artifact
     * @return Multiplication
     */
    @Contract(pure = true)
    @Override
    public String visit(Times p, Void ignored) {
        return "multiplication";
    }

    /**
     * Division
     * @param p Division
     * @param ignored Visitor pattern artifact
     * @return Division
     */
    @Contract(pure = true)
    @Override
    public String visit(Div p, Void ignored) {
        return "division";
    }

    /**
     * Modulo
     * @param p Modulo
     * @param ignored Visitor pattern artifact
     * @return Modulo
     */
    @Contract(pure = true)
    @Override
    public String visit(Mod p, Void ignored) {
        return "modulo";
    }
}
