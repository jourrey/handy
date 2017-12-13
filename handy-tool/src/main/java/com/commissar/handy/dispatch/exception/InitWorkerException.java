package com.commissar.handy.dispatch.exception;

/**
 * {@link RuntimeException} which is thrown when an init Worker operation fails.
 *
 * @author babu
 */
public class InitWorkerException extends RuntimeException {
    private static final long serialVersionUID = 6112741709874861143L;

    /**
     * Creates a new exception.
     */
    public InitWorkerException() {
    }

    /**
     * Creates a new exception.
     */
    public InitWorkerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     */
    public InitWorkerException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     */
    public InitWorkerException(Throwable cause) {
        super(cause);
    }

}