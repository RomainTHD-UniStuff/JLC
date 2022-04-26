package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.Visitor;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Bool;
import javalette.Absyn.Doub;
import javalette.Absyn.Int;
import javalette.Absyn.Prog;
import javalette.Absyn.Type;
import javalette.Absyn.Void;

import java.io.FileWriter;
import java.io.IOException;

import static fr.rthd.jlc.TypeCode.CBool;
import static fr.rthd.jlc.TypeCode.CDouble;
import static fr.rthd.jlc.TypeCode.CInt;
import static fr.rthd.jlc.TypeCode.CVoid;

/**
 * Compiler
 * @author RomainTHD
 */
public class LLVMCompiler implements Visitor {
    private final String _outputFile;

    public LLVMCompiler(String output) {
        this._outputFile = output;
    }

    /**
     * Javalette type from TypeCode
     * @param type TypeCode type
     * @return Javalette type
     * @throws IllegalArgumentException If type is not supported
     * @see TypeCode
     */
    private static Type javaletteTypeFromTypecode(TypeCode type) throws IllegalArgumentException {
        if (CInt.equals(type)) {
            return new Int();
        } else if (CDouble.equals(type)) {
            return new Doub();
        } else if (CBool.equals(type)) {
            return new Bool();
        } else if (CVoid.equals(type)) {
            return new Void();
        } else {
            throw new IllegalArgumentException(
                "Unsupported type: " +
                type
            );
        }
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

        if (this._outputFile == null) {
            System.out.println(asm);
        } else {
            try {
                FileWriter fw = new FileWriter(this._outputFile);
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
