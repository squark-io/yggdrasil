package io.squark.yggdrasil.core.api.exception;

/**
 * *** Yggdrasil ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-09.
 * Copyright 2016
 */
public class DependencyResolutionException extends YggdrasilException {

    public DependencyResolutionException(String message) {
        super(message);
    }

    public DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyResolutionException(Throwable cause) {
        super(cause);
    }
}
