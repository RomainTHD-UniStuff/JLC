package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.TypeCode;
import javalette.Absyn.Ass;
import javalette.Absyn.EVar;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;
import javalette.Absyn.Stmt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Declaration visitor
 * @author RomainTHD
 */
@NonNls
class ItemVisitor implements Item.Visitor<Item, EnvTypecheck> {
    /**
     * Variable type
     */
    @NotNull
    private final TypeCode _varType;

    /**
     * Constructor
     * @param varType Variable type
     */
    public ItemVisitor(@NotNull TypeCode varType) {
        _varType = varType;
    }

    /**
     * Declaration only
     * @param p Declaration
     * @param env Environment
     * @return Declaration
     */
    @Override
    public NoInit visit(NoInit p, EnvTypecheck env) {
        env.insertVar(p.ident_, _varType);
        return new NoInit(p.ident_);
    }

    /**
     * Declaration with initialization
     * @param p Declaration
     * @param env Environment
     * @return Declaration
     */
    @Override
    public Init visit(Init p, EnvTypecheck env) {
        env.insertVar(p.ident_, _varType);
        Stmt s = new Ass(
            new EVar(p.ident_),
            p.expr_
        ).accept(new StmtVisitor(), env);
        return new Init(p.ident_, ((Ass) s).expr_2);
    }
}
