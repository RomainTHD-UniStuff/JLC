package fr.rthd.jlc.env.exception;

import org.jetbrains.annotations.Nls;

/**
 * Generic environment exception
 * @author RomainTHD
 */
@Nls
public class EnvException extends RuntimeException {
    protected EnvException(String msg) {
        super(msg);
    }
}
