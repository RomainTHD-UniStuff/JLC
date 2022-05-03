package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import org.jetbrains.annotations.NonNls;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class definition visitor
 * @author RomainTHD
 */
@NonNls
public class ClassDefVisitor implements ClassDef.Visitor<Void, EnvCompiler> {
    /**
     * Class definition
     * @param p Class definition
     * @param env Environment
     */
    @Override
    public Void visit(ClsDef p, EnvCompiler env) {
        ClassType c = env.lookupClass(p.ident_);
        assert c != null;

        List<Attribute> attrs = c.getAllAttributes();
        // Comment like `Class Foo: int bar`
        env.emit(env.instructionBuilder.comment(String.format(
            "Class %s: %s",
            p.ident_,
            attrs.stream()
                 .map(Attribute::toString)
                 .collect(Collectors.joining(", "))
        )));
        // Define class
        env.emit(env.instructionBuilder.classDef(
            c.getName(),
            attrs.stream()
                 .map(Attribute::getType)
                 .collect(Collectors.toList())
        ));
        env.emit(env.instructionBuilder.newLine());

        // Set class as current class and visit class body
        env.setCurrentClass(c);
        p.listmember_.forEach(member -> member.accept(
            new MemberVisitor(),
            env
        ));
        env.setCurrentClass(null);

        return null;
    }
}
