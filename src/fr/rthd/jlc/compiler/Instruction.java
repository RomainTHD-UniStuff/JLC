package fr.rthd.jlc.compiler;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    private final List<String> _commands;
    public boolean indentable;

    public Instruction() {
        this._commands = new ArrayList<>();
        this.indentable = true;
    }

    public Instruction(String command) {
        this();
        this.add(command);
    }

    protected void add(Instruction inst) {
        this._commands.addAll(inst.emit());
    }

    protected void add(String command) {
        this._commands.add(command);
    }

    public List<String> emit() {
        return _commands;
    }
}
