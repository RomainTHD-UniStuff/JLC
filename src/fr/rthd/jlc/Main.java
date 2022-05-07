package fr.rthd.jlc;

import fr.rthd.jlc.compiler.llvm.LLVMCompiler;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.Env;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.env.exception.EnvException;
import fr.rthd.jlc.internal.Unannotater;
import fr.rthd.jlc.optimizer.Optimizer;
import fr.rthd.jlc.typechecker.TypeChecker;
import fr.rthd.jlc.typechecker.exception.TypeException;
import javalette.Absyn.Prog;
import javalette.PrettyPrinter;
import javalette.Yylex;
import javalette.parser;
import org.jetbrains.annotations.Nls;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Main class of the compiler
 * @author RomainTHD
 * @see ArgParse
 */
@Nls
public class Main {
    /**
     * @return Help message
     */
    private static String getHelp() {
        String[] lines = {
            "JLC - a Java-like compiler",
            "By RomainTHD, 2022, under GPLv3",
            "",
            "Usage: jlc [<file>]",
            "\t[-o|--output <file>]",
            "\t[-b|--backend x86 | amd64|x86_64|x64 | llvm | riscv]",
            "\t([-q|--quiet] | [--error] | [--warn] | [-v|--info|--verbose] | [-vv|--debug|--very-verbose])",
            "\t([-Oz] | [-Os] | [-0|--O0] | [-1|--O1] | [-2|--O2] | [-3|--O3])",
            "\t[-t|--typecheck-only|--typecheck]",
            "\t[-h|--help]",
            "",
            "Options:",
            "\t-o, --output <file>\t\t\tOutput file name",
            "\t-b, --backend x86 | amd64|x86_64|x64 | llvm | jvm|java | riscv\tBackend to use",
            "\t-q, --quiet\t\t\t\tQuiet mode",
            "\t--error\t\t\t\t\tOnly show errors",
            "\t--warn\t\t\t\t\tShow warnings",
            "\t-v, --info, --verbose\t\t\tShow info",
            "\t-vv, --debug, --very-verbose\t\tShow debug",
            "\t-t, --typecheck-only, --typecheck\tOnly typecheck",
            "\t--ast, --ast-only\t\t\t\tOnly print AST",
            "\t-h, --help\t\t\t\tShow this help",
            "\t-Oz, -Os, -0, --O0, --O1, --O2, --O3\tOptimization level",
            "",
        };

        return Arrays
            .stream(lines)
            .reduce((a, b) -> a + "\n" + b)
            .get();
    }

    /**
     * Exit the program
     * @param status Status code
     */
    private static void exit(int status) {
        if (status == 0) {
            System.err.println("OK");
        }

        System.exit(status);
    }

    /**
     * Main method
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        ArgParse opt = ArgParse.parse(args);

        if (opt.showHelp) {
            // If help is requested, print it and exit
            System.out.println(getHelp());
            System.exit(0);
        }

        String lexerInput = null;

        if (opt.inputFile != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(opt.inputFile));
                StringBuilder input = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    input.append(line).append("\n");
                }
                reader.close();
                lexerInput = input.toString();
            } catch (IOException ignored) {
                // TODO: Log this error
            }
        }

        if (lexerInput == null) {
            StringBuilder input = new StringBuilder();
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                input.append(sc.nextLine()).append("\n");
            }

            lexerInput = input.toString();
        }

        // HACK: The grammar doesn't support `int [ ] t;`, so we replace it with
        //  `int [] t;`
        lexerInput = lexerInput.replaceAll("\\[[\\t\\s \\n\\r]+]", "[]");
        Yylex lex = new Yylex(new StringReader(lexerInput));

        try {
            // Parse
            parser p = new parser(lex);
            Prog tree = p.pProg();

            // Type check
            Env<?, FunType, ClassType> env = new Env<>();
            tree = new TypeChecker().accept(tree, env);

            if (opt.typecheckOnly) {
                exit(0);
            }

            if (opt.optimizationLevel != 0) {
                tree = new Optimizer(opt.optimizationLevel).accept(tree, env);
            }

            if (opt.printAST) {
                // TODO: Respect the -o flag
                System.out.println(PrettyPrinter.print(new Unannotater().accept(
                    tree,
                    env
                )));
                exit(0);
            }

            if (opt.backend == ArgParse.Backend.LLVM) {
                tree = new LLVMCompiler(opt.outputFile).accept(tree, env);
            } else {
                throw new UnsupportedOperationException(
                    "Backend not supported yet"
                );
            }

            exit(0);
        } catch (TypeException e) {
            System.err.println("ERROR");
            System.err.println("Type error: " + e.getMessage());
            exit(1);
        } catch (EnvException e) {
            System.err.println("ERROR");
            System.err.println("Environment error: " + e.getMessage());
            exit(1);
        } catch (RuntimeException | StackOverflowError | AssertionError e) {
            e.printStackTrace();
            exit(-1);
        } catch (IOException e) {
            System.err.println("ERROR");
            System.err.println("IO error: " + e.getMessage());
            exit(1);
        } catch (Exception | Error e) {
            System.err.println("ERROR");
            System.err.println(
                "Syntax error at line " +
                lex.line_num() +
                ", near \"" +
                lex.buff() +
                "\" :"
            );
            System.err.println("\t" + e.getMessage());
            exit(1);
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
