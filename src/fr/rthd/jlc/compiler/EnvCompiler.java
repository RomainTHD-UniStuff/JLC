package fr.rthd.jlc.compiler;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class EnvCompiler extends Env<Variable, FunType> {
    public static final String INDENT = "\t";
    public static final char SEP = '$';

    private final List<String> _output;
    private final LinkedList<Map<String, Integer>> _varCount;
    private final LinkedList<Map<String, Integer>> _labelCount;
    private final MessageDigest _hashAlgorithm;
    private int _indentLevel;

    public EnvCompiler(Env<?, FunType> env) {
        super(env);
        this._output = new ArrayList<>();
        this._varCount = new LinkedList<>();
        this._varCount.push(new HashMap<>());
        this._indentLevel = 0;
        this._labelCount = new LinkedList<>();
        this._labelCount.push(new HashMap<>());

        try {
            this._hashAlgorithm = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        return String.format(
            "stack_%d%cscope_%d",
            _varCount.size() - 1,
            SEP,
            count
        );
    }

    public Variable createTempVar(TypeCode type, String ctx) {
        return new Variable(type, String.format(
            ".temp%c%s%c%s",
            SEP,
            ctx,
            SEP,
            getVariableUID(ctx)
        ));
    }

    public Variable createVar(TypeCode type, String name) {
        return new Variable(type, String.format(
            "%s%c%s",
            name,
            SEP,
            getVariableUID(name)
        ));
    }

    public Variable createGlobalStringLiteral(String content) {
        return new Variable(TypeCode.CString, String.format(
            ".string%c%s",
            SEP,
            getHash(content)
        ), true, content.length() + 1);
    }

    public String getNewLabel(String ctx) {
        Map<String, Integer> scope = _labelCount.peek();
        assert scope != null;
        int count = scope.getOrDefault(ctx, 0);
        scope.put(ctx, count + 1);
        return String.format(
            ".label%c%s%cstack_%d%cscope_%d",
            SEP,
            ctx,
            SEP,
            _varCount.size() - 1,
            SEP,
            count
        );
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

    private String getHash(String content) {
        _hashAlgorithm.update(content.getBytes());
        byte[] bytes = _hashAlgorithm.digest();
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
