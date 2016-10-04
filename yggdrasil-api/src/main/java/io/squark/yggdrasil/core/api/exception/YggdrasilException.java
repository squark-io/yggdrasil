package io.squark.yggdrasil.core.api.exception;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public class YggdrasilException extends Exception {
    public YggdrasilException(String message) {
        super(message);
    }

    public YggdrasilException(String message, Throwable cause) {
        super(message, cause);
    }

    public YggdrasilException(Throwable cause) {
        super(cause);
    }

    public YggdrasilException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    private YggdrasilException() {

    }
}
