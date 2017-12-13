package com.commissar.handy.feeding.exception;

/**
 * {@link Exception} which is thrown when an not found resources operation fails.
 *
 * @author babu
 */
public class UnavailableResourcesException extends Exception {
    private static final long serialVersionUID = 2908618315971075004L;

    /**
     * Creates a new exception.
     */
    public UnavailableResourcesException() {
    }

    /**
     * Creates a new exception.
     */
    public UnavailableResourcesException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     */
    public UnavailableResourcesException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     */
    public UnavailableResourcesException(Throwable cause) {
        super(cause);
    }
}