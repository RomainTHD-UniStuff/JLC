package fr.rthd.jlc.typechecker.exception;

/**
 * Self found outside a class
 * @author RomainTHD
 */
public class SelfOutOfClassException extends TypeException {
    public SelfOutOfClassException() {
        super("`self` found outside a class");
    }
}
