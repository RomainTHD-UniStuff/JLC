package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.AnnotatedExpr;
import fr.rthd.jlc.TypeCode;
import javalette.Absyn.EVar;
import javalette.Absyn.Init;
import javalette.Absyn.Item;
import javalette.Absyn.NoInit;

import static fr.rthd.jlc.optimizer.Optimizer.isLiteral;

class ItemVisitor implements Item.Visitor<Item, EnvOptimizer> {
    private final TypeCode _varType;

    public ItemVisitor(TypeCode varType) {
        _varType = varType;
    }

    public NoInit visit(NoInit s, EnvOptimizer env) {
        // We need to force the insertion because of the following edge case:
        //  ```c
        //  int x = 0;
        //  if (unknown) {
        //      x++; // A new value for x is inserted
        //      int x = 1; // Double insert, will throw an exception
        //  }
        //  ```

        env.insertVar(
            s.ident_,
            AnnotatedExpr.getDefaultValue(_varType),
            true
        );
        return new NoInit(s.ident_);
    }

    public Init visit(Init s, EnvOptimizer env) {
        AnnotatedExpr<?> exp = s.expr_.accept(new ExprVisitor(), env);
        if (isLiteral(exp)) {
            env.insertVar(
                s.ident_,
                exp,
                true
            );
        } else {
            env.insertVar(
                s.ident_,
                new AnnotatedExpr<>(
                    exp.getType(),
                    new EVar(s.ident_)
                ),
                true
            );
        }

        return new Init(s.ident_, exp);
    }
}
