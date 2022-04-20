package fr.rthd.jlc.env;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, FunType> _methods;

    /**
     * List of defined fields
     */
    private final Map<String, Attribute> _attributes;

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
        this._methods = new HashMap<>();
        this._attributes = new HashMap<>();
    }

    public boolean addMethod(FunType f) {
        if (this._methods.containsKey(f.name)) {
            return false;
        }

        this._methods.put(f.name, f);
        return true;
    }

    public Collection<FunType> getMethods() {
        return this._methods.values();
    }

    public boolean addAttribute(Attribute a) {
        if (this._attributes.containsKey(a.name)) {
            return false;
        }

        this._attributes.put(a.name, a);
        return true;
    }

    public Collection<Attribute> getAttributes() {
        return this._attributes.values();
    }

    public void updateSuperclass(ClassType c) {
        this._superclass = c;
    }

    public ClassType getSuperclass() {
        return _superclass;
    }

    public boolean isCastableTo(ClassType c) {
        if (this.equals(c)) {
            return true;
        }
        if (this.superclassName == null) {
            return false;
        }
        ClassType superclass = this.getSuperclass();
        return superclass.isCastableTo(c);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof ClassType)) {
            return false;
        } else {
            ClassType c = (ClassType) obj;
            return this.name.equals(c.name);
        }
    }

    @Override
    public String toString() {
        return "ClassType{" +
               "name=" +
               name +
               ", superclass=" +
               superclassName +
               ", methods=" +
               _methods +
               ", attributes=" +
               _attributes +
               "}";
    }
}
