package main.Exceptions;

/**
 * Thrown when the object is uploaded to an index that doesn't correspond to a supported datatype
 */
public class InvalidIndexException extends Exception {
    /**
     * Creates an instance of InvalidIndexException
     * @param errorMessage is the error message
     */
    public InvalidIndexException(String errorMessage) {
        super(errorMessage);
    }
}
