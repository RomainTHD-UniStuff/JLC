package fr.rthd.jlc.env;

import fr.rthd.jlc.TypeCode;

public class FunArg {
    public final TypeCode type;
    public final String name;

    private String _generatedName;

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
