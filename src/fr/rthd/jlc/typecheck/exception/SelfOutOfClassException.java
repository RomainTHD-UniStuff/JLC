package fr.rthd.jlc.typecheck.exception;

public class SelfOutOfClassException extends TypeException {
    public SelfOutOfClassException() {
        super("`self` found outside a class");
    }
}
