package fr.rthd.jlc.compiler.llvm;

import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;

import java.util.List;
import java.util.stream.Collectors;

public class ClassDefVisitor implements ClassDef.Visitor<Void, EnvCompiler> {
    @Override
    public Void visit(ClsDef p, EnvCompiler env) {
        ClassType c = env.lookupClass(p.ident_);

        List<Attribute> attrs = c.getAllAttributes();
        env.emit(env.instructionBuilder.comment(String.format(
            "Class %s: %s",
            p.ident_,
            attrs.stream()
                 .map(Attribute::toString)
                 .collect(Collectors.joining(", "))
        )));
        env.emit(env.instructionBuilder.classDef(
            c.getName(),
            attrs.stream()
                 .map(attr -> attr.getType())
                 .collect(Collectors.toList())
        ));
        env.emit(env.instructionBuilder.newLine());

        env.setCurrentClass(c);
        p.listmember_.forEach(member -> member.accept(
            new MemberVisitor(),
            env
        ));
        env.setCurrentClass(null);

        return null;
    }
}
