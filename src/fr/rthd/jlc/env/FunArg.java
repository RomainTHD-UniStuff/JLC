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
    public final TypeCode type;

    /**
     * Argument logical name, like `x` for `f(x)`
     */
    public final String name;

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
        this.type = type;
        this.name = name;
    }

    public String getGeneratedName() {
        return _generatedName;
    }

    public void setGeneratedName(String name) {
        this._generatedName = name;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
