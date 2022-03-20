package fr.rthd.jlc;

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
 * Main class of the Javalette compiler.
 */
public class Main {
    /**
     * Main method
     * @param args Command line arguments. Here, `--backend <Backend>
     *     <SourceFile>`
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: fr.rthd.jlc.jlc --backend <Backend>");
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
            Prog parseTree = p.pProg();

            // Type check
            Prog typedTree = new TypeChecker().typecheck(parseTree);

            // Generate code
            // TODO: Generate code for the backend

            System.err.println("OK");
        } catch (TypeException e) {
            System.err.println("ERROR");
            System.err.println("Type error: " + e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
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