package fr.rthd.jlc;

import java.util.Arrays;
import java.util.List;

/**
 * Argument parser
 * @author RomainTHD
 */
public class ArgParse {
    /**
     * Show help or not.
     * Flag -h or --help
     */
    public final boolean showHelp;

    /**
     * Verbose mode.
     * Flags:
     * -q or --quiet
     * --error
     * --warn
     * -v, --verbose or --info
     * -vv, --very-verbose or --debug
     */
    public final VerboseLevel verbosity;

    /**
     * Optimization level, negative for size, positive for speed.
     * Flags:
     * -Oz
     * -Os
     * -O0 or -0
     * -O1 or -1
     * -O2 or -2
     * -O3 or -3
     */
    public final int optimizationLevel;

    /**
     * Typechecking only
     * Flag -t, --typecheck or --typecheck-only
     */
    public final boolean typecheckOnly;

    /**
     * Input file, stdin if not specified
     */
    public final String inputFile;

    /**
     * Output file, stdout if not specified
     * Flag -o or --output <file>
     */
    public final String outputFile;

    /**
     * Specified backend
     * Flag -b or --backend <backend>,
     * where backend is one of:
     * - llvm (default)
     * - riscv
     * - x86
     * - x86_64, amd64 or x64
     * - jvm or java
     */
    public final Backend backend;

    /**
     * Print the AST or not
     * Flag --ast or --print-ast
     */
    public final boolean printAST;

    /**
     * Constructor, builder pattern
     * @param showHelp Show help or not
     * @param verbosity Verbosity level
     * @param optimizationLevel Optimization level
     * @param typecheckOnly Typechecking only
     * @param inputFile Input file
     * @param outputFile Output file
     * @param backend Specified backend
     * @param printAST Print the AST or not
     */
    private ArgParse(
        boolean showHelp,
        VerboseLevel verbosity,
        int optimizationLevel,
        boolean typecheckOnly,
        String inputFile,
        String outputFile,
        Backend backend,
        boolean printAST
    ) {
        this.showHelp = showHelp;
        this.verbosity = verbosity;
        this.optimizationLevel = optimizationLevel;
        this.typecheckOnly = typecheckOnly;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.backend = backend;
        this.printAST = printAST;
    }

    /**
     * Parse arguments
     * @param args Arguments
     * @return Parsed arguments
     */
    public static ArgParse parse(String[] args) {
        return parse(Arrays.asList(args));
    }

    /**
     * Parse arguments
     * @param args Arguments
     * @return Parsed arguments
     */
    public static ArgParse parse(List<String> args) {
        boolean showHelp = false;
        VerboseLevel verbosity = VerboseLevel.WARNING;
        int optimizationLevel = 0;
        boolean typecheckOnly = false;
        String inputFile = null;
        String outputFile = null;
        Backend backend = Backend.LLVM;
        boolean printAST = false;

        String linkedFlag = null;
        for (String arg : args) {
            boolean processed = true;

            switch (arg) {
                // VERBOSITY

                case "-q":
                case "--quiet":
                    verbosity = VerboseLevel.SILENT;
                    break;

                case "--error":
                    verbosity = VerboseLevel.ERROR;
                    break;

                case "--warn":
                    verbosity = VerboseLevel.WARNING;
                    break;

                case "-v":
                case "--verbose":
                case "--info":
                    verbosity = VerboseLevel.INFO;
                    break;

                case "-vv":
                case "--very-verbose":
                case "--debug":
                    verbosity = VerboseLevel.DEBUG;
                    break;

                // OPTIMIZATION LEVEL

                case "-0z":
                    optimizationLevel = -2;
                    break;

                case "-0s":
                    optimizationLevel = -1;
                    break;

                case "-0":
                case "--O0":
                    optimizationLevel = 0;
                    break;

                case "-1":
                case "--O1":
                    optimizationLevel = 1;
                    break;

                case "-2":
                case "--O2":
                    optimizationLevel = 2;
                    break;

                case "-3":
                case "--O3":
                    optimizationLevel = 3;
                    break;

                // MISC

                case "--typecheck-only":
                case "--typecheck":
                case "-t":
                    typecheckOnly = true;
                    break;

                case "--print-ast":
                case "--ast":
                    printAST = true;
                    break;

                case "-h":
                case "--help":
                    showHelp = true;
                    break;

                // DEFAULT

                default:
                    processed = false;
                    break;
            }

            if (processed) {
                // Standalone flags
                if (linkedFlag != null) {
                    // A situation like `jlc -o --help`
                    throw new IllegalArgumentException(String.format(
                        "Invalid argument combination between %s and %s",
                        linkedFlag,
                        arg
                    ));
                }
                continue;
            }

            if (arg.startsWith("-")) {
                linkedFlag = arg;
                continue;
            }

            if (linkedFlag == null) {
                // A situation like `jlc <file>`
                inputFile = arg;
                continue;
            }

            switch (linkedFlag) {
                case "-o":
                case "--output":
                    outputFile = arg;
                    break;

                case "-b":
                case "--backend":
                    switch (arg.toLowerCase()) {
                        case "llvm":
                            backend = Backend.LLVM;
                            break;

                        case "x86":
                            backend = Backend.X86;
                            break;

                        case "x64":
                        case "x86_64":
                        case "amd64":
                            backend = Backend.X86_64;
                            break;

                        case "riscv":
                            backend = Backend.RISCV;
                            break;

                        case "jvm":
                        case "java":
                            backend = Backend.JVM;
                            break;

                        default:
                            throw new IllegalArgumentException(String.format(
                                "Unknown backend: %s",
                                arg
                            ));
                    }
                    break;

                default:
                    throw new IllegalArgumentException(String.format(
                        "Invalid argument combination between %s and %s",
                        linkedFlag,
                        arg
                    ));
            }

            linkedFlag = null;
        }

        return new ArgParse(
            showHelp,
            verbosity,
            optimizationLevel,
            typecheckOnly,
            inputFile,
            outputFile,
            backend,
            printAST
        );
    }

    /**
     * Verbose level
     */
    public enum VerboseLevel {
        SILENT,
        ERROR,
        WARNING,
        INFO,
        DEBUG,
    }

    /**
     * Available backends
     */
    public enum Backend {
        LLVM,
        X86,
        X86_64,
        RISCV,
        JVM,
    }
}
