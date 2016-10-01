package io.squark.ask.core.api.exception;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public class AskException extends Exception {
    public AskException(String message) {
        super(message);
    }

    public AskException(String message, Throwable cause) {
        super(message, cause);
    }

    public AskException(Throwable cause) {
        super(cause);
    }

    public AskException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    private AskException() {

    }
}
