package com.commissar.handy.dispatch.exception;

/**
 * {@link RuntimeException} which is thrown when an operation Worker fails.
 *
 * @author babu
 */
public class OperationWorkerException extends RuntimeException {
    private static final long serialVersionUID = 6112741709874861143L;

    /**
     * Creates a new exception.
     */
    public OperationWorkerException() {
    }

    /**
     * Creates a new exception.
     */
    public OperationWorkerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     */
    public OperationWorkerException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     */
    public OperationWorkerException(Throwable cause) {
        super(cause);
    }

}