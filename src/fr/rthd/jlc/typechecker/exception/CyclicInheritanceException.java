package fr.rthd.jlc.typechecker.exception;

public class CyclicInheritanceException extends TypeException {
    public CyclicInheritanceException(String base, String superclass) {
        super(String.format(
            "Cyclic inheritance between `%s` and `%s`",
            base,
            superclass
        ));
    }
}
