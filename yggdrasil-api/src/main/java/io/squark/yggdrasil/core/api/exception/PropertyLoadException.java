package io.squark.yggdrasil.core.api.exception;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class PropertyLoadException extends YggdrasilException {

    public PropertyLoadException(String message) {
        super(message);
    }

    public PropertyLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyLoadException(Throwable cause) {
        super(cause);
    }

    public PropertyLoadException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
