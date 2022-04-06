package fr.rthd.jlc;

import fr.rthd.jlc.compiler.Compiler;
import fr.rthd.jlc.compiler.llvm.LLVMInstructionBuilder;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.env.exception.EnvException;
import fr.rthd.jlc.optimizer.Optimizer;
import fr.rthd.jlc.typecheck.TypeChecker;
import fr.rthd.jlc.typecheck.exception.TypeException;
import javalette.Absyn.Prog;
import javalette.Yylex;
import javalette.parser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

/**
 * Main class of the compiler
 * @author RomainTHD
 */
public class Main {
    /**
     * Main method
     * @param args Command line arguments. Here, `--backend <Backend>`
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: fr.rthd.jlc.Main --backend <Backend>");
            System.exit(1);
        }

        String backend = args[1].toLowerCase().replaceAll("-+", "");

        StringBuilder input = new StringBuilder();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            input.append(sc.nextLine()).append("\n");
        }

        Yylex lex = null;
        try {
            // Parse
            lex = new Yylex(new StringReader(input.toString()));
            parser p = new parser(lex);
            Prog tree = p.pProg();

            // Type check
            Env<?, FunType> env = new Env<>();
            tree = new TypeChecker().typecheck(tree, env);
            tree = new Optimizer().optimize(tree, env);
            System.out.println(new Compiler(
                new LLVMInstructionBuilder()
            ).compile(tree, env));
            System.err.println("OK");
        } catch (TypeException e) {
            System.err.println("ERROR");
            System.err.println("Type error: " + e.getMessage());
            System.exit(1);
        } catch (EnvException e) {
            System.err.println("ERROR");
            System.err.println("Environment error: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException | StackOverflowError e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("ERROR");
            System.err.println("IO error: " + e.getMessage());
            System.exit(1);
        } catch (Throwable e) {
            assert lex != null;
            System.err.println("ERROR");
            System.err.println(
                "Syntax error at line " +
                lex.line_num() +
                ", near \"" +
                lex.buff() +
                "\" :"
            );
            System.err.println("     " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Remove path from a file name
     * @param path Path
     * @return File name
     */
    private static String stripPath(String path) {
        return new File(path).getName();
    }

    /**
     * Remove the file name from a path. Returns "." if there was no path
     * @param path Path
     * @return Path without file name
     */
    private static String stripFileName(String path) {
        String dir = new File(path).getParent();
        if (dir == null) {
            dir = ".";
        }
        return dir;
    }

    /**
     * Remove extension from a file name
     * @param filename File name
     * @return File name without extension
     */
    private static String stripSuffix(String filename) {
        int divider = filename.lastIndexOf('.');
        if (divider <= 0) {
            return filename;
        } else {
            return filename.substring(0, divider);
        }
    }
}
