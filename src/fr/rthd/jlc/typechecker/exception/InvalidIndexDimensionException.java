package fr.rthd.jlc.typechecker.exception;

/**
 * Index too deep, like `(new int[10])[0][0]`
 * @author RomainTHD
 */
public class InvalidIndexDimensionException extends TypeException {
    public InvalidIndexDimensionException(int baseDim, int accessDim) {
        super(String.format(
            "Trying to access dimension %d of a %d-dimensions array",
            accessDim,
            baseDim
        ));
    }
}
