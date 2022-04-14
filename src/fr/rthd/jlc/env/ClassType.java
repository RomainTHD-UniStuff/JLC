package fr.rthd.jlc.env;

import java.util.List;

/**
 * Class representation
 * @author RomainTHD
 */
public class ClassType {
    /**
     * Class name
     */
    public final String name;

    /**
     * Superclass or null
     */
    public final String superclass;

    /**
     * List of defined methods
     */
    public final List<FunType> methods;

    /**
     * List of defined fields
     */
    public final List<Attribute> attributes;

    /**
     * Constructor
     * @param name Class name
     * @param superclass Superclass or null
     * @param methods List of defined methods
     * @param attributes List of defined fields
     */
    public ClassType(
        String name,
        String superclass,
        List<FunType> methods,
        List<Attribute> attributes
    ) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "ClassType{" +
               "name=" +
               name +
               ", superclass=" +
               superclass +
               ", methods=" +
               methods +
               ", attributes=" +
               attributes +
               "}";
    }
}
