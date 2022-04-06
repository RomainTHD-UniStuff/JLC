package fr.rthd.jlc.env.exception;

/**
 * Generic environment exception
 * @author RomainTHD
 */
public class EnvException extends RuntimeException {
    protected EnvException(String msg) {
        super(msg);
    }
}
