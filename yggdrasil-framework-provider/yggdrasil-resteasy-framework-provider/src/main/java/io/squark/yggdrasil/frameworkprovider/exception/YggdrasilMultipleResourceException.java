package io.squark.yggdrasil.frameworkprovider.exception;

import io.squark.yggdrasil.core.api.exception.YggdrasilException;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-04-01.
 * Copyright 2016
 */
public class YggdrasilMultipleResourceException extends YggdrasilException {
    public YggdrasilMultipleResourceException(String message) {
        super(message);
    }

    public YggdrasilMultipleResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public YggdrasilMultipleResourceException(Throwable cause) {
        super(cause);
    }

    public YggdrasilMultipleResourceException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
