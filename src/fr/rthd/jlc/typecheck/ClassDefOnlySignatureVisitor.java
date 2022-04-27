package fr.rthd.jlc.typecheck;

import fr.rthd.jlc.env.ClassType;
import javalette.Absyn.ClassDef;
import javalette.Absyn.ClsDef;
import javalette.Absyn.HBase;
import javalette.Absyn.HExtends;

/**
 * We need to visit the class definition twice: first we list all the classes
 * (here), then we fill them with their attributes and methods. This is because
 * a function returning an object could see this object not recognized
 * initially
 * @author RomainTHD
 * @see ClassDefSignatureVisitor
 */
class ClassDefOnlySignatureVisitor implements ClassDef.Visitor<Void, EnvTypecheck> {
    public Void visit(ClsDef p, EnvTypecheck env) {
        String superclass;
        if (p.classinheritance_ instanceof HBase) {
            superclass = null;
        } else if (p.classinheritance_ instanceof HExtends) {
            superclass = ((HExtends) p.classinheritance_).ident_;
        } else {
            throw new IllegalArgumentException(String.format(
                "Unknown interhitance type: %s",
                p.classinheritance_.getClass().getName()
            ));
        }

        env.insertClass(new ClassType(
            p.ident_,
            superclass
        ));

        return null;
    }
}