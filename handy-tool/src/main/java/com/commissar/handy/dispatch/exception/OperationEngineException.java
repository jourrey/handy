package com.commissar.handy.dispatch.exception;

/**
 * {@link RuntimeException} which is thrown when an operation Engine fails.
 *
 * @author babu
 */
public class OperationEngineException extends RuntimeException {
    private static final long serialVersionUID = -8340160008373053818L;

    /**
     * Creates a new exception.
     */
    public OperationEngineException() {
    }

    /**
     * Creates a new exception.
     */
    public OperationEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     */
    public OperationEngineException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     */
    public OperationEngineException(Throwable cause) {
        super(cause);
    }

}