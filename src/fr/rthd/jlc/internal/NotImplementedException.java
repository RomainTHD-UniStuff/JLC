package fr.rthd.jlc.internal;

public class NotImplementedException extends UnsupportedOperationException {
    public NotImplementedException() {
        super("ERROR: Not implemented yet");
    }

    public NotImplementedException(String msg) {
        super("ERROR: Not implemented yet. " + msg);
    }
}
