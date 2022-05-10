package fr.rthd.jlc.utils;

/**
 * LValue or RValue enum
 * @author RomainTHD
 */
public enum Value {
    /**
     * Modifiable, like a variable. Persistent in memory.
     * Example: v in `v = 0`, `v++`, etc
     */
    LValue,

    /**
     * Not modifiable, content, is a constant. Used as an intermediate value.
     * Example: v in `f(v)`, `x = v + 1`, etc
     */
    RValue,
}
