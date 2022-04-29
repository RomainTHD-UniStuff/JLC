package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representation
 * @author RomainTHD
 */
@NonNls
public class ClassType {
    /**
     * Class name
     */
    @NotNull
    private final String _name;

    /**
     * Superclass name or null
     * @see #_superclass
     */
    @Nullable
    private final String _superclassName;

    /**
     * List of defined methods
     */
    @NotNull
    private final Map<String, FunType> _methods;

    /**
     * List of defined fields
     */
    @NotNull
    private final Map<String, Attribute> _attributes;

    /**
     * Superclass or null. Will be definer later on, when all classes have been
     * discovered
     */
    @Nullable
    private ClassType _superclass = null;

    /**
     * Constructor for inheritance
     * @param name Class name
     * @param superclassName Superclass name or null
     */
    public ClassType(
        @NotNull String name,
        @Nullable String superclassName
    ) {
        _name = name;
        _superclassName = superclassName;
        _methods = new HashMap<>();
        _attributes = new HashMap<>();
    }

    public void addMethod(@NotNull FunType f) {
        _methods.put(f.getName(), f);
    }

    @NotNull
    public Collection<FunType> getOwnMethods() {
        return _methods.values();
    }

    @NotNull
    public Collection<FunType> getAllMethods() {
        Collection<FunType> methods = new ArrayList<>(getOwnMethods());
        if (_superclass != null) {
            methods.addAll(_superclass.getAllMethods());
        }
        return methods;
    }

    @Nullable
    public FunType getMethod(@NotNull String name) {
        if (_methods.containsKey(name)) {
            return _methods.get(name);
        } else if (_superclass != null) {
            return _superclass.getMethod(name);
        } else {
            return null;
        }
    }

    public void addAttribute(@NotNull Attribute a) {
        _attributes.put(a.getName(), a);
    }

    @NotNull
    public Collection<Attribute> getOwnAttributes() {
        return _attributes.values();
    }

    @NotNull
    public List<Attribute> getAllAttributes() {
        List<Attribute> attrs = new ArrayList<>();
        if (_superclass != null) {
            attrs.addAll(_superclass.getAllAttributes());
        }
        attrs.addAll(getOwnAttributes());
        return attrs;
    }

    public boolean hasAttribute(@NotNull String name) {
        if (_attributes.containsKey(name)) {
            return true;
        } else if (_superclass != null) {
            return _superclass.hasAttribute(name);
        } else {
            return false;
        }
    }

    public boolean hasMethod(@NotNull String name) {
        if (_methods.containsKey(name)) {
            return true;
        } else if (_superclass != null) {
            return _superclass.hasMethod(name);
        } else {
            return false;
        }
    }

    public void updateSuperclass(@Nullable ClassType c) {
        _superclass = c;
    }

    @Nullable
    public ClassType getSuperclass() {
        return _superclass;
    }

    public boolean isCastableTo(@NotNull ClassType c) {
        if (equals(c)) {
            return true;
        }
        if (_superclass == null) {
            return false;
        }
        return _superclass.isCastableTo(c);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof ClassType)) {
            return false;
        } else {
            ClassType c = (ClassType) obj;
            return _name.equals(c._name);
        }
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return "ClassType{" +
               "name=" +
               _name +
               ", superclass=" +
               (_superclass == null ? "null" : _superclass.getName()) +
               ", methods=" +
               _methods +
               ", attributes=" +
               _attributes +
               "}";
    }

    @NotNull
    @Contract(pure = true)
    public String getConstructorName() {
        return "__constructor";
    }

    @NotNull
    public TypeCode getType() {
        return TypeCode.forClass(_name);
    }

    @NotNull
    @Contract(pure = true)
    public String getName() {
        return _name;
    }

    @Nullable
    @Contract(pure = true)
    public String getSuperclassName() {
        return _superclassName;
    }

    @NotNull
    @Contract(pure = true)
    public String getAssemblyMethodName(@NotNull String funcName) {
        return _name + "$" + funcName;
    }
}
