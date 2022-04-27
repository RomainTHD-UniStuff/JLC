package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

/**
 * Function argument
 * @author RomainTHD
 */
public class FunArg {
    /**
     * Argument type
     */
    private final TypeCode _type;

    /**
     * Argument logical name, like `x` for `f(x)`
     */
    private final String _name;

    /**
     * Argument generated name, like `x$0$0` for `f(x)`
     */
    private String _generatedName;

    /**
     * Constructor
     * @param type Argument type
     * @param name Argument name
     */
    public FunArg(TypeCode type, String name) {
        _type = type;
        _name = name;
    }

    public String getGeneratedName() {
        return _generatedName;
    }

    public void setGeneratedName(String name) {
        _generatedName = name;
    }

    @Override
    public String toString() {
        return String.format(
            "%s %s",
            _type.getRealName(),
            _name
        );
    }

    public TypeCode getType() {
        return _type;
    }

    public String getName() {
        return _name;
    }
}
