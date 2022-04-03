package fr.rthd.jlc.env.exception;

public class SymbolNotFoundException extends EnvException {
    public SymbolNotFoundException(String symbol) {
        super(String.format(
            "Symbol `%s` not found",
            symbol
        ));
    }
}
