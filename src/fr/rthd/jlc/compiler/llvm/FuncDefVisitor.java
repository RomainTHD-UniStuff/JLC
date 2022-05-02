package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.TypeCode;
import fr.rthd.jlc.compiler.Literal;
import fr.rthd.jlc.compiler.Variable;
import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunArg;
import fr.rthd.jlc.env.FunType;
import javalette.Absyn.EVar;
import javalette.Absyn.FnDef;
import javalette.Absyn.FuncDef;
import javalette.Absyn.Init;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

/**
 * Function definition visitor
 * @author RomainTHD
 */
@NonNls
class FuncDefVisitor implements FuncDef.Visitor<Void, EnvCompiler> {
    /**
     * Function definition
     * @param p Function definition
     * @param env Environment
     */
    @Override
    public Void visit(FnDef p, EnvCompiler env) {
        FunType func;

        ClassType c = env.getCurrentClass();
        if (c == null) {
            // Look for global function
            func = env.lookupFun(p.ident_);
        } else {
            // Look for method
            // FIXME: Should already be resolved since ProgVisitor adds all
            //  methods to the global environment
            func = c.getMethod(p.ident_);
        }

        assert func != null;

        env.resetScope();

        if (c != null) {
            // `this` is the first argument for methods
            func.addArgFirst(new FunArg(c.getType(), "this"));
        }

        List<Variable> args = new ArrayList<>();
        func.getArgs().forEach(arg -> {
            Variable var = env.createVar(
                arg.getType(),
                arg.getName(),
                arg.getType().isPrimitive() ? 0 : 1
                // Primitive types are passed by value
            );
            env.insertVar(arg.getName(), var);
            args.add(var);
        });

        env.emit(env.instructionBuilder.functionDeclarationStart(
            c,
            func.getRetType(),
            func.getName(),
            args
        ));
        env.emit(env.instructionBuilder.label("entry"));

        for (FunArg arg : func.getArgs()) {
            Variable v = env.lookupVar(arg.getName());
            assert v != null;
            if (v.getPointerLevel() == 0) {
                // Arguments are passed by value, so we load them to respect the
                //  convention that all variables are pointers. Otherwise, the
                //  following code would fail: `void f(int x) { x++; }`
                new Init(
                    arg.getName(),
                    new EVar(arg.getName())
                ).accept(new ItemVisitor(
                    arg.getType(),
                    true
                ), env);
            }
        }

        if (c != null) {
            // If method, we load all fields on the stack
            List<Attribute> attrs = c.getAllAttributes();
            for (int i = 0; i < attrs.size(); i++) {
                Attribute a = attrs.get(i);
                Variable v = env.createVar(
                    a.getType(),
                    a.getName(),
                    a.getType().isPrimitive() ? 1 : 2,
                    true
                );
                env.insertVar(a.getName(), v);
                Variable thisVar = env.lookupVar("this");
                assert thisVar != null;
                env.emit(env.instructionBuilder.loadAttribute(v, thisVar, i));
            }
        }

        p.blk_.accept(new BlkVisitor(), env);

        // Add return instruction in case it wasn't already present
        // FIXME: Check if it was actually already present
        if (func.getRetType() == TypeCode.CVoid) {
            env.emit(env.instructionBuilder.ret());
        } else {
            env.emit(env.instructionBuilder.ret(new Literal(
                func.getRetType(),
                func.getRetType().getDefaultValue(),
                func.getRetType().isPrimitive() ? 0 : 1
            )));
        }

        env.emit(env.instructionBuilder.functionDeclarationEnd());
        env.emit(env.instructionBuilder.newLine());

        return null;
    }
}
