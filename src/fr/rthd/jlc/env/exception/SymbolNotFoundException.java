package fr.rthd.jlc.env.exception;

/**
 * Symbol not found
 * @author RomainTHD
 */
public class SymbolNotFoundException extends EnvException {
    public SymbolNotFoundException(String symbol) {
        super(String.format(
            "Symbol `%s` not found",
            symbol
        ));
    }
}
