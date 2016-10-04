package io.squark.yggdrasil.core.api.exception;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-03-19.
 * Copyright 2016
 */
public class MainClassLoadException extends YggdrasilException {
    public MainClassLoadException(String message) {
        super(message);
    }

    public MainClassLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public MainClassLoadException(Throwable cause) {
        super(cause);
    }

    public MainClassLoadException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
