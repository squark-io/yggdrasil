package io.hakansson.dynamicjar.frameworkprovider.exception;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class CDIException extends DynamicJarException {
    public CDIException(String message) {
        super(message);
    }

    public CDIException(String message, Throwable cause) {
        super(message, cause);
    }

    public CDIException(Throwable cause) {
        super(cause);
    }

    public CDIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
