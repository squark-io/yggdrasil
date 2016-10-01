package io.squark.dynamicjar.core.api.exception;

/**
 * Created by Erik Håkansson on 2016-05-31.
 * WirelessCar
 */
public class FrameworkProviderException extends DynamicJarException {

    public FrameworkProviderException(String message) {
        super(message);
    }

    public FrameworkProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameworkProviderException(Throwable cause) {
        super(cause);
    }

    public FrameworkProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
