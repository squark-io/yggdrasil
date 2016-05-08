package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public interface LoggingModule {
    void initialize(@Nullable URI configuration, @Nullable String rootLogLevel)
    throws DynamicJarException;
}
