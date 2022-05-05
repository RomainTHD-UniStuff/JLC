package fr.rthd.jlc.typechecker.exception;

import org.jetbrains.annotations.Nls;

/**
 * Type exception
 * @author RomainTHD
 */
@Nls
public class TypeException extends RuntimeException {
    protected TypeException(String msg) {
        super(msg);
    }
}
