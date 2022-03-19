package fr.rthd.jlc.typecheck.exception;

public class SymbolAlreadyDefinedException extends TypeException {
    public SymbolAlreadyDefinedException(String symbolName) {
        super(String.format("Symbol `%s` already defined", symbolName));
    }
}
