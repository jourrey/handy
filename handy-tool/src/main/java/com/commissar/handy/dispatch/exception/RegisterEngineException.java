package com.commissar.handy.dispatch.exception;

/**
 * {@link RuntimeException} which is thrown when an register Engine operation fails.
 *
 * @author babu
 */
public class RegisterEngineException extends RuntimeException {
    private static final long serialVersionUID = 4601603614721772392L;

    /**
     * Creates a new exception.
     */
    public RegisterEngineException() {
    }

    /**
     * Creates a new exception.
     */
    public RegisterEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     */
    public RegisterEngineException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     */
    public RegisterEngineException(Throwable cause) {
        super(cause);
    }

}