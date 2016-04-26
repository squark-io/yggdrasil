package io.hakansson.dynamicjar.core.api.exception;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-11.
 * Copyright 2016
 */
public class NestedJarClassloaderException extends DynamicJarException {
    public NestedJarClassloaderException(String message) {
        super(message);
    }

    public NestedJarClassloaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NestedJarClassloaderException(Throwable cause) {
        super(cause);
    }

    public NestedJarClassloaderException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NestedJarClassloaderException() {
    }
}
