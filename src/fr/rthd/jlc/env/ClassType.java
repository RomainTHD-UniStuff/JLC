package fr.rthd.jlc.env;

import java.util.ArrayList;
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
     * Constructor for inheritance
     * @param name Class name
     * @param superclass Superclass or null
     */
    public ClassType(
        String name,
        String superclass
    ) {
        this.name = name;
        this.superclass = superclass;
        this.methods = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    public void addMethod(FunType f) {
        this.methods.add(f);
    }

    public void addAttribute(Attribute a) {
        this.attributes.add(a);
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
