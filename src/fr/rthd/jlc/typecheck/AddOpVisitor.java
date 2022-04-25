package fr.rthd.jlc.typecheck;

import javalette.Absyn.AddOp;
import javalette.Absyn.Minus;
import javalette.Absyn.Plus;

class AddOpVisitor implements AddOp.Visitor<String, Void> {
    public String visit(Plus p, Void ignored) {
        return "addition";
    }

    public String visit(Minus p, Void ignored) {
        return "subtraction";
    }
}
