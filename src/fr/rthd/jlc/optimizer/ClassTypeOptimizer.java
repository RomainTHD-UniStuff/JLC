package fr.rthd.jlc.optimizer;

import fr.rthd.jlc.env.Attribute;
import fr.rthd.jlc.env.ClassType;
import fr.rthd.jlc.env.FunType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ClassTypeOptimizer extends ClassType<FunTypeOptimizer> {
    /**
     * Constructor for inheritance
     * @param name Class name
     * @param superclassName Superclass name or null
     */
    public ClassTypeOptimizer(
        @NotNull String name,
        @Nullable String superclassName
    ) {
        super(name, superclassName);
    }

    public ClassTypeOptimizer(ClassType<?> cls) {
        super(cls.getName(), cls.getSuperclassName());
        for (FunType f : cls.getOwnMethods()) {
            addMethod(new FunTypeOptimizer(f), false);
        }
        for (Attribute a : cls.getOwnAttributes()) {
            addAttribute(a);
        }
    }
}
