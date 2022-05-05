package fr.rthd.jlc.typechecker.exception;

public class SelfOutOfClassException extends TypeException {
    public SelfOutOfClassException() {
        super("`self` found outside a class");
    }
}
