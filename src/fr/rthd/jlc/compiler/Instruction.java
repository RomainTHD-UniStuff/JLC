package fr.rthd.jlc.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Instruction
 * @author RomainTHD
 */
public class Instruction {
    /**
     * List of commands
     */
    private final List<String> _commands;

    /**
     * Indentable instruction or not
     */
    private boolean _indentable;

    /**
     * Constructor
     */
    public Instruction() {
        this._commands = new ArrayList<>();
        this._indentable = true;
    }

    /**
     * Constructor
     * @param command Command
     */
    public Instruction(String command) {
        this();
        this.add(command);
    }

    /**
     * Add an instruction
     * @param inst Instruction
     */
    protected void add(Instruction inst) {
        this._commands.addAll(inst.emit());
    }

    /**
     * Add a command
     * @param command Command
     */
    protected void add(String command) {
        this._commands.add(command);
    }

    /**
     * Emit the instruction
     * @return Instruction as string
     */
    public List<String> emit() {
        return _commands;
    }

    public boolean isIndentable() {
        return _indentable;
    }

    public void setIndentable(boolean indentable) {
        this._indentable = indentable;
    }
}
