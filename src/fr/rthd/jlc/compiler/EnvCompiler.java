package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvCompiler extends Env<Variable, FunType> {
    public static final String INDENT = "\t";

    private final List<String> output;
    private final Map<String, Integer> varCount;
    private int indentLevel;

    public EnvCompiler(Env<?, FunType> env) {
        super(env);
        this.output = new ArrayList<>();
        this.varCount = new HashMap<>();
        this.indentLevel = 0;
    }

    public String toAssembly() {
        StringBuilder res = new StringBuilder();
        for (String inst : output) {
            res.append(inst).append("\n");
        }

        return res.toString();
    }

    public void indent() {
        ++this.indentLevel;
    }

    public void unindent() {
        --this.indentLevel;
    }

    private String getIndentString() {
        return INDENT.repeat(indentLevel);
    }

    public void emit(Instruction inst) {
        for (String emitted : inst.emit()) {
            if (emitted.isEmpty()) {
                output.add("");
            } else if (inst.indentable) {
                output.add(getIndentString() + emitted);
            } else {
                output.add(emitted);
            }
        }
    }

    protected void emitRaw(String command) {
        output.add(getIndentString() + command);
    }

    protected void emitRaw(int index, String command) {
        output.add(index, getIndentString() + command);
    }

    public void emitNewLine() {
        output.add("");
    }

    private int getVarCount(String name) {
        int count = varCount.getOrDefault(name, 0);
        varCount.put(name, count + 1);
        return count;
    }

    public Variable createTempVar(TypeCode type, String ctx) {
        return new Variable(type, String.format(
            "_temp_%s_%d",
            ctx,
            getVarCount(ctx)
        ));
    }

    public Variable createVar(TypeCode type, String name) {
        return new Variable(type, String.format(
            "%s_%d",
            name,
            getVarCount(name)
        ));
    }
}
