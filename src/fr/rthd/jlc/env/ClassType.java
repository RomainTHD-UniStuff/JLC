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
     * Superclass name or null
     * @see #_superclass
     */
    public final String superclassName;
    /**
     * List of defined methods
     */
    public final List<FunType> methods;
    /**
     * List of defined fields
     */
    public final List<Attribute> attributes;
    /**
     * Superclass or null. Will be definer later on, when all classes have been
     * discovered
     */
    private ClassType _superclass = null;

    /**
     * Constructor for inheritance
     * @param name Class name
     * @param superclassName Superclass name or null
     */
    public ClassType(
        String name,
        String superclassName
    ) {
        this.name = name;
        this.superclassName = superclassName;
        this.methods = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    public void addMethod(FunType f) {
        this.methods.add(f);
    }

    public void addAttribute(Attribute a) {
        this.attributes.add(a);
    }

    public void updateSuperclass(ClassType c) {
        this._superclass = c;
    }

    public ClassType getSuperclass() {
        return _superclass;
    }

    @Override
    public String toString() {
        return "ClassType{" +
               "name=" +
               name +
               ", superclass=" +
               superclassName +
               ", methods=" +
               methods +
               ", attributes=" +
               attributes +
               "}";
    }
}
