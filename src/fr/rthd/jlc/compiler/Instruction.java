package fr.rthd.jlc.compiler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Instruction
 * @author RomainTHD
 */
@NonNls
public class Instruction {
    /**
     * List of commands
     */
    @NotNull
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
    public Instruction(@NotNull String command) {
        this();
        add(command);
    }

    /**
     * Add an instruction
     * @param inst Instruction
     */
    protected void add(@NotNull Instruction inst) {
        _commands.addAll(inst.emit());
    }

    /**
     * Add a command
     * @param command Command
     */
    protected void add(@NotNull String command) {
        _commands.add(command);
    }

    /**
     * Emit the instruction
     * @return List of instructions
     */
    @Contract(pure = true)
    @NotNull
    public List<String> emit() {
        return _commands;
    }

    @Contract(pure = true)
    public boolean isIndentable() {
        return _indentable;
    }

    public void setIndentable(boolean indentable) {
        _indentable = indentable;
    }
}
