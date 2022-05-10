package fr.rthd.jlc.typechecker;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import fr.rthd.jlc.typechecker.exception.CyclicInheritanceException;
import fr.rthd.jlc.typechecker.exception.InvalidArgumentCountException;
import fr.rthd.jlc.typechecker.exception.InvalidReturnedTypeException;
import fr.rthd.jlc.typechecker.exception.NoSuchClassException;
import fr.rthd.jlc.typechecker.exception.NoSuchFunctionException;
import fr.rthd.jlc.utils.Choice;
import javalette.Absyn.ListTopDef;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Program signature visitor
 * @author RomainTHD
 */
@NonNls
class ProgSignatureVisitor implements Prog.Visitor<Prog, EnvTypecheck> {
    /**
     * Check the signature of all the functions, methods and attributes
     * @param p Program
     * @param env Environment
     * @return Program with the signature checked
     */
    @Override
    public Prog visit(Program p, EnvTypecheck env) {
        ListTopDef listTopDef = p.listtopdef_;
        listAllClasses(listTopDef, env);
        updateSuperclasses(env);
        checkCycles(env);
        listFunctions(listTopDef, env);
        addConstructors(listTopDef, env);
        addExternalFunctions(env);
        checkMain(env);
        return new Program(listTopDef);
    }

    /**
     * List all the classes in the program
     * @param listTopDef List of top definitions
     * @param env Environment
     */
    private void listAllClasses(
        @NotNull ListTopDef listTopDef,
        @NotNull EnvTypecheck env
    ) {
        for (TopDef def : listTopDef) {
            def.accept(new TopDefClassDefSignatureVisitor(), env);
        }
    }

    /**
     * Update all the classes' superclass with a java object reference
     * @param env Environment
     */
    private void updateSuperclasses(@NotNull EnvTypecheck env) {
        for (ClassType c : env.getAllClass()) {
            if (c.getSuperclassName() == null) {
                c.updateSuperclass(null);
            } else {
                ClassType superclass = env.lookupClass(c.getSuperclassName());
                if (superclass == null) {
                    throw new NoSuchClassException(c.getSuperclassName());
                }
                c.updateSuperclass(superclass);
            }
        }
    }

    /**
     * Check for cyclic inheritance
     * @param env Environment
     */
    private void checkCycles(@NotNull EnvTypecheck env) {
        for (ClassType c : env.getAllClass()) {
            ClassType superclass = c;
            do {
                if (c.equals(superclass.getSuperclass())) {
                    throw new CyclicInheritanceException(
                        superclass.getName(),
                        c.getName()
                    );
                }
                superclass = superclass.getSuperclass();
            } while (superclass != null);
        }
    }

    /**
     * List all the functions, methods and attributes
     * @param listTopDef List of top definitions
     * @param env Environment
     */
    private void listFunctions(
        @NotNull ListTopDef listTopDef,
        @NotNull EnvTypecheck env
    ) {
        for (TopDef def : listTopDef) {
            def.accept(new TopDefSignatureVisitor(), env);
        }
    }

    /**
     * Add the constructors to the classes
     * @param listTopDef List of top definitions
     * @param env Environment
     */
    private void addConstructors(
        @NotNull ListTopDef listTopDef,
        @NotNull EnvTypecheck env
    ) {
        for (TopDef def : listTopDef) {
            def.accept(new TopDefConstructorSignatureVisitor(), env);
        }
    }

    /**
     * Add the external functions to the environment, like `printInt`
     * @param env Environment
     */
    private void addExternalFunctions(@NotNull EnvTypecheck env) {
        env.insertFun(new FunType(
            TypeCode.CVoid,
            "printInt",
            new FunArg(TypeCode.CInt, "i")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CVoid,
            "printDouble",
            new FunArg(TypeCode.CDouble, "d")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CVoid,
            "printString",
            new FunArg(TypeCode.CString, "s")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CInt,
            "readInt"
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CDouble,
            "readDouble"
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CRawPointer,
            "malloc",
            new FunArg(TypeCode.CInt, "size")
        ).setExternal().setPure(Choice.FALSE));

        env.insertFun(new FunType(
            TypeCode.CRawPointer,
            "calloc",
            new FunArg(TypeCode.CInt, "nitems"),
            new FunArg(TypeCode.CInt, "size")
        ).setExternal().setPure(Choice.FALSE));
    }

    /**
     * Check everything related to the main function of the program
     * @param env Environment
     */
    private void checkMain(@NotNull EnvTypecheck env) {
        // Check that main exists
        FunType mainFunc = env.lookupFun("main");
        if (mainFunc == null) {
            throw new NoSuchFunctionException("main");
        } else {
            mainFunc.setAsMain();
        }

        // Check that it returns an int
        if (mainFunc.getRetType() != TypeCode.CInt) {
            throw new InvalidReturnedTypeException(
                "main",
                TypeCode.CInt,
                mainFunc.getRetType()
            );
        }

        // Check that it has no arguments
        if (!mainFunc.getArgs().isEmpty()) {
            throw new InvalidArgumentCountException(
                "main",
                0,
                mainFunc.getArgs().size()
            );
        }
    }
}
