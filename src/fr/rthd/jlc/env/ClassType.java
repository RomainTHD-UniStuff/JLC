package fr.rthd.jlc.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    public void addMethod(FunType f) {
        this._methods.put(f.name, f);
    }

    public Collection<FunType> getOwnMethods() {
        return this._methods.values();
    }

    public Collection<FunType> getAllMethods() {
        Collection<FunType> methods = new ArrayList<>(this.getOwnMethods());
        if (this.superclassName != null) {
            methods.addAll(this.getSuperclass().getAllMethods());
        }
        return methods;
    }

    public FunType getMethod(String name) {
        if (this._methods.containsKey(name)) {
            return this._methods.get(name);
        } else if (this.superclassName != null) {
            return this.getSuperclass().getMethod(name);
        } else {
            return null;
        }
    }

    public void addAttribute(Attribute a) {
        this._attributes.put(a.name, a);
    }

    public Collection<Attribute> getOwnAttributes() {
        return this._attributes.values();
    }

    public List<Attribute> getAllAttributes() {
        List<Attribute> attrs = new ArrayList<>();
        if (this.superclassName != null) {
            attrs.addAll(this.getSuperclass().getAllAttributes());
        }
        attrs.addAll(this.getOwnAttributes());
        return attrs;
    }

    public boolean hasAttribute(String name) {
        if (this._attributes.containsKey(name)) {
            return true;
        } else if (this.superclassName != null) {
            return this.getSuperclass().hasAttribute(name);
        } else {
            return false;
        }
    }

    public boolean hasMethod(String name) {
        if (this._methods.containsKey(name)) {
            return true;
        } else if (this.superclassName != null) {
            return this.getSuperclass().hasMethod(name);
        } else {
            return false;
        }
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

    public String getConstructorName() {
        return "__constructor";
    }
}
