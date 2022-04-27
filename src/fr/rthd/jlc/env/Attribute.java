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
    private final TypeCode _type;

    /**
     * Attribute logical name, like `x` for `self.x`
     */
    private final String _name;

    /**
     * Constructor
     * @param type Attribute type
     * @param name Attribute name
     */
    public Attribute(TypeCode type, String name) {
        this._type = type;
        this._name = name;
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
