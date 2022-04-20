package fr.rthd.jlc.typecheck.exception;

public class CyclicInheritanceException extends TypeException {
    public CyclicInheritanceException(String base, String superclass) {
        super("Cyclic inheritance between " + base + " and " + superclass);
    }
}
