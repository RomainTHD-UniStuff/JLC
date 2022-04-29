package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.Ass;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import javalette.Absyn.Stmt;
import org.jetbrains.annotations.NotNull;

class ItemVisitor implements Item.Visitor<Item, EnvTypecheck> {
    @NotNull
    private final TypeCode _varType;

    public ItemVisitor(@NotNull TypeCode varType) {
        _varType = varType;
    }

    public NoInit visit(NoInit p, EnvTypecheck env) {
        env.insertVar(p.ident_, _varType);
        return new NoInit(p.ident_);
    }

    public Init visit(Init p, EnvTypecheck env) {
        env.insertVar(p.ident_, _varType);
        Stmt s = new Ass(p.ident_, p.expr_).accept(new StmtVisitor(), env);
        return new Init(p.ident_, ((Ass) s).expr_);
    }
}
