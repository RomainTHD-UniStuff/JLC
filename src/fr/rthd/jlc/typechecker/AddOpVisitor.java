package fr.rthd.jlc.typechecker;

import javalette.Absyn.AddOp;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nls;

/**
 * Operator visitor for `+` and `-`
 * @author RomainTHD
 */
@Nls
class AddOpVisitor implements AddOp.Visitor<String, Void> {
    /**
     * Visit `+`
     * @param p `+`
     * @param ignored Visitor pattern artifact
     * @return `"addition"`
     */
    @Contract(pure = true)
    @Override
    public String visit(Plus p, Void ignored) {
        return "addition";
    }

    /**
     * Visit `-`
     * @param p `-`
     * @param ignored Visitor pattern artifact
     * @return `"subtraction"`
     */
    @Contract(pure = true)
    @Override
    public String visit(Minus p, Void ignored) {
        return "subtraction";
    }
}
