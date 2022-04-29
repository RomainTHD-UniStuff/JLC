package fr.rthd.jlc.internal;

import org.jetbrains.annotations.Nls;

/**
 * Not implemented exception
 */
@Nls
public class NotImplementedException extends UnsupportedOperationException {
    public NotImplementedException() {
        super("ERROR: Not implemented yet");
    }

    public NotImplementedException(String msg) {
        super("ERROR: Not implemented yet. " + msg);
    }
}
