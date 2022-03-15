import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class FunArg {
    public final TypeCode type;
    public final String name;

    public FunArg(TypeCode type, String name) {
        this.type = type;
        this.name = name;
    }
}

class FunType {
    public final TypeCode retType;
    public final List<FunArg> args;

    public FunType(TypeCode retVal, FunArg... args) {
        this(retVal, Arrays.asList(args));
    }

    public FunType(TypeCode retVal, List<FunArg> args) {
        this.retType = retVal;
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder argsTypes = new StringBuilder();
        for (FunArg typeArg : args) {
            argsTypes.append(typeArg.name)
                     .append(":")
                     .append(typeArg.type)
                     .append(" ");
        }

        return this.retType.name() + " <- " + argsTypes;
    }
}

public class Env<Value, Func> {
    private final Map<String, Func> _signature;
    private final LinkedList<Map<String, Value>> _contexts;

    public Env() {
        this._signature = new HashMap<>();
        this._contexts = new LinkedList<>();
    }

    public Env(Env<?, Func> baseEnv) {
        this._signature = baseEnv._signature;
        this._contexts = new LinkedList<>();
        this._contexts.add(new HashMap<>());
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("\n");
        for (String funcName : _signature.keySet()) {
            s.append(funcName).append(" ");
            s.append(lookupFun(funcName));
            s.append("\n");
        }

        Iterator<Map<String, Value>> iter = _contexts.descendingIterator();
        while (iter.hasNext()) {
            Map<String, Value> ctx = iter.next();
            s.append("\n");
            for (String varName : ctx.keySet()) {
                s.append(varName).append(" ");
                s.append(lookupVar(varName));
                s.append("\n");
            }
        }

        return s.toString();
    }

    public Value lookupVar(String id) {
        for (Map<String, Value> env : _contexts) {
            Value v = env.get(id);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    public Func lookupFun(String id) {
        return _signature.get(id);
    }

    public void insertVar(String id, Value value) throws TypeException {
        assert value != null;
        Map<String, Value> env = _contexts.peek();
        assert env != null;
        if (env.get(id) == null) {
            env.put(id, value);
        } else {
            throw new SymbolAlreadyDefinedException(id);
        }
    }

    public void updateVar(String id, Value value) {
        for (Map<String, Value> env : _contexts) {
            Value v = env.get(id);
            if (v != null) {
                env.put(id, value);
                break;
            }
        }
    }

    public void insertFun(String id, Func func) throws TypeException {
        if (lookupFun(id) == null) {
            _signature.put(id, func);
        } else {
            throw new SymbolAlreadyDefinedException(id);
        }
    }

    public void enterScope() {
        _contexts.push(new HashMap<>());
    }

    public void leaveScope() {
        _contexts.pop();
    }

    public void resetScope() {
        _contexts.clear();
    }
}
