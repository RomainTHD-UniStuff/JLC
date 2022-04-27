package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Compiler
 * @author RomainTHD
 */
public class LLVMCompiler implements Visitor {
    private final String _outputFile;

    public LLVMCompiler(String output) {
        _outputFile = output;
    }

    /**
     * Entry point
     * @param p Program
     * @param parent Parent environment
     * @return Compiled program as a string
     */
    public Prog accept(Prog p, Env<?, FunType, ClassType> parent) {
        EnvCompiler env = new EnvCompiler(parent, new InstructionBuilder());
        p.accept(new ProgVisitor(), env);
        String asm = env.toAssembly();

        if (_outputFile == null) {
            System.out.println(asm);
        } else {
            try {
                FileWriter fw = new FileWriter(_outputFile);
                fw.write(asm);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Log error
            }
        }

        return p;
    }
}
