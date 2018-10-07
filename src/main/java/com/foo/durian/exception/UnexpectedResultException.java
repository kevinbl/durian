package com.foo.durian.exception;

/**
 * Version 1.0.0
 * Created by f on 16/11/22.
 */
public class UnexpectedResultException extends RuntimeException {
    private static final long serialVersionUID = 5745137852781726060L;

    public UnexpectedResultException() {}

    public UnexpectedResultException(String message) {
        super(message);
    }

    public UnexpectedResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
