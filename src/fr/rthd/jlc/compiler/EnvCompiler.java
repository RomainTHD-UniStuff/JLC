package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EnvCompiler extends Env<Variable, FunType> {
    public static final String INDENT = "\t";

    private final List<String> _output;
    private final LinkedList<Map<String, Integer>> _varCount;
    private final LinkedList<Map<String, Integer>> _labelCount;
    private int _indentLevel;

    public EnvCompiler(Env<?, FunType> env) {
        super(env);
        this._output = new ArrayList<>();
        this._varCount = new LinkedList<>();
        this._varCount.push(new HashMap<>());
        this._indentLevel = 0;
        this._labelCount = new LinkedList<>();
        this._labelCount.push(new HashMap<>());
    }

    public String toAssembly() {
        StringBuilder res = new StringBuilder();
        for (String inst : _output) {
            res.append(inst).append("\n");
        }

        return res.toString();
    }

    public void indent() {
        ++this._indentLevel;
    }

    public void unindent() {
        --this._indentLevel;
    }

    private String getIndentString() {
        return INDENT.repeat(_indentLevel);
    }

    public void emit(Instruction inst) {
        for (String emitted : inst.emit()) {
            if (emitted.isEmpty()) {
                _output.add("");
            } else if (inst.indentable) {
                _output.add(getIndentString() + emitted);
            } else {
                _output.add(emitted);
            }
        }
    }

    public void emitAtBeginning(Instruction inst) {
        for (String emitted : inst.emit()) {
            if (emitted.isEmpty()) {
                _output.add(0, "");
            } else if (inst.indentable) {
                _output.add(0, getIndentString() + emitted);
            } else {
                _output.add(0, emitted);
            }
        }
    }

    private String getVariableUID(String name) {
        Map<String, Integer> scope = _varCount.peek();
        assert scope != null;
        int count = scope.getOrDefault(name, 0);
        scope.put(name, count + 1);
        return String.format("%d_%d", _varCount.size() - 1, count);
    }

    public Variable createTempVar(TypeCode type, String ctx) {
        return new Variable(type, String.format(
            "_temp_%s_%s",
            ctx,
            getVariableUID(ctx)
        ));
    }

    public Variable createVar(TypeCode type, String name) {
        return new Variable(type, String.format(
            "%s_%s",
            name,
            getVariableUID(name)
        ));
    }

    public String getNewLabel(String ctx) {
        Map<String, Integer> scope = _labelCount.peek();
        assert scope != null;
        int count = scope.getOrDefault(ctx, 0);
        scope.put(ctx, count + 1);
        return String.format("_label_%s_%d_%d", ctx, _varCount.size() - 1, count);
    }

    @Override
    public void enterScope() {
        super.enterScope();
        _varCount.push(new HashMap<>());
    }

    @Override
    public void leaveScope() {
        super.leaveScope();
        _varCount.pop();
    }

    @Override
    public void resetScope() {
        super.resetScope();
        _varCount.clear();
        _varCount.push(new HashMap<>());
    }
}
