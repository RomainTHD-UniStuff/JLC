package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.Prog;
import javalette.Absyn.Program;
import javalette.Absyn.TopDef;
import javalette.Absyn.Void;
import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.Map;

/**
 * Program visitor
 * @author RomainTHD
 */
@NonNls
class ProgVisitor implements Prog.Visitor<Void, EnvCompiler> {
    /**
     * Program visitor
     * @param p Program
     * @param env Environment
     */
    @Override
    public Void visit(Program p, EnvCompiler env) {
        env.emit(env.instructionBuilder.newLine());

        for (FunType fun : env.getAllFun()) {
            if (fun.isExternal()) {
                // External functions are not emitted, only declared
                env.emit(env.instructionBuilder.declareExternalFunction(fun));
            }
        }

        Map<String, FunType> classFunctions = new HashMap<>();
        for (ClassType c : env.getAllClass()) {
            // Fill the environment with the class methods, since they now
            //  have a unique assembly name
            for (FunType f : c.getAllMethods()) {
                classFunctions.put(c.getAssemblyMethodName(f.getName()), f);
            }
        }

        // FIXME: Sill useful since all functions are now declared as global?
        env.setClassFunctions(classFunctions);

        env.emit(env.instructionBuilder.newLine());

        for (TopDef topdef : p.listtopdef_) {
            topdef.accept(new TopDefVisitor(), env);
        }

        env.setClassFunctions(null);
        return null;
    }
}
