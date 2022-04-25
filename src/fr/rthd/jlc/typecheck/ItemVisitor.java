package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.Ass;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import javalette.Absyn.Stmt;

class ItemVisitor implements Item.Visitor<Item, EnvTypecheck> {
    private final TypeCode varType;

    public ItemVisitor(TypeCode varType) {
        this.varType = varType;
    }

    public NoInit visit(NoInit p, EnvTypecheck env) {
        env.insertVar(p.ident_, varType);
        return new NoInit(p.ident_);
    }

    public Init visit(Init p, EnvTypecheck env) {
        env.insertVar(p.ident_, varType);
        Stmt s = new Ass(p.ident_, p.expr_).accept(new StmtVisitor(), env);
        return new Init(p.ident_, ((Ass) s).expr_);
    }
}
