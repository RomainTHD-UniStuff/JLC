package fr.rthd.jlc.typecheck;

import javalette.Absyn.Div;
import javalette.Absyn.Mod;
import javalette.Absyn.MulOp;
import javalette.Absyn.Times;

class MulOpVisitor implements MulOp.Visitor<String, Void> {
    public String visit(Times p, Void ignored) {
        return "multiplication";
    }

    public String visit(Div p, Void ignored) {
        return "division";
    }

    public String visit(Mod p, Void ignored) {
        return "modulo";
    }
}
