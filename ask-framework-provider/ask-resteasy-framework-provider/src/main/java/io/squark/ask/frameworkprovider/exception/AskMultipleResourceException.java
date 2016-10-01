package io.squark.ask.frameworkprovider.exception;

import io.squark.ask.core.api.exception.AskException;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-04-01.
 * Copyright 2016
 */
public class AskMultipleResourceException extends AskException {
    public AskMultipleResourceException(String message) {
        super(message);
    }

    public AskMultipleResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AskMultipleResourceException(Throwable cause) {
        super(cause);
    }

    public AskMultipleResourceException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
