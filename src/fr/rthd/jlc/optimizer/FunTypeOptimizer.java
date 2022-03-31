package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;

import java.util.List;

public class FunTypeOptimizer extends FunType {
    private boolean _isUsed;

    public FunTypeOptimizer(
        TypeCode retVal,
        String name,
        boolean external,
        FunArg... args
    ) {
        super(retVal, name, external, args);
    }

    public FunTypeOptimizer(
        TypeCode retVal,
        String name,
        boolean external,
        List<FunArg> args
    ) {
        super(retVal, name, external, args);
    }
}
