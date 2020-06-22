package main.Exceptions;

/**
 * Thrown when the metadata provided is incomplete
 */
public class InvalidMetadataException extends Exception {
    /**
     * Creates an instance of InavlideMetadataException
     * @param errorMessage is the error message
     */
    public InvalidMetadataException(String errorMessage) {
        super(errorMessage);
    }
}
