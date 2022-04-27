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
        _commands = new ArrayList<>();
        _indentable = true;
    }

    /**
     * Constructor
     * @param command Command
     */
    public Instruction(String command) {
        this();
        add(command);
    }

    /**
     * Add an instruction
     * @param inst Instruction
     */
    protected void add(Instruction inst) {
        _commands.addAll(inst.emit());
    }

    /**
     * Add a command
     * @param command Command
     */
    protected void add(String command) {
        _commands.add(command);
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
        _indentable = indentable;
    }
}
