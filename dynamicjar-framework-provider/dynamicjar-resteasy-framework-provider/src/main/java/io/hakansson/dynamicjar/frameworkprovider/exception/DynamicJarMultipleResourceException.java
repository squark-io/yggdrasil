package io.hakansson.dynamicjar.frameworkprovider.exception;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-04-01.
 * Copyright 2016
 */
public class DynamicJarMultipleResourceException extends DynamicJarException {
    public DynamicJarMultipleResourceException(String message) {
        super(message);
    }

    public DynamicJarMultipleResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicJarMultipleResourceException(Throwable cause) {
        super(cause);
    }

    public DynamicJarMultipleResourceException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DynamicJarMultipleResourceException() {
    }
}
