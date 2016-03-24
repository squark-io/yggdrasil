package io.hakansson.dynamicjar.core.api.exception;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public abstract class DynamicJarException extends Exception {
    public DynamicJarException(String message) {
        super(message);
    }

    public DynamicJarException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicJarException(Throwable cause) {
        super(cause);
    }

    public DynamicJarException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DynamicJarException() {

    }
}
