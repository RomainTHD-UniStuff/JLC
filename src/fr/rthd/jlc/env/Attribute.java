package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

/**
 * Class attribute
 * @author RomainTHD
 */
public class Attribute {
    /**
     * Attribute type
     */
    public final TypeCode type;

    /**
     * Attribute logical name, like `x` for `self.x`
     */
    public final String name;

    /**
     * Constructor
     * @param type Attribute type
     * @param name Attribute name
     */
    public Attribute(TypeCode type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
