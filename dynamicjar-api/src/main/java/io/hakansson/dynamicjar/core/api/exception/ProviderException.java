package io.hakansson.dynamicjar.core.api.exception;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-12.
 * Copyright 2016
 */
public class ProviderException extends DynamicJarException {
    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderException(Throwable cause) {
        super(cause);
    }

    public ProviderException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ProviderException() {
        super();
    }
}
