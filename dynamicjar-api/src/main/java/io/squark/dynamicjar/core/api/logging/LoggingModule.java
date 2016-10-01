package io.squark.dynamicjar.core.api.logging;

import io.squark.dynamicjar.core.api.exception.DynamicJarException;
import io.squark.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;

import java.net.URL;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public interface LoggingModule {
    ILoggerFactory initialize(@Nullable DynamicJarConfiguration configuration, @Nullable ClassLoader classLoader, @Nullable URL jarWithConfig)
    throws DynamicJarException;
}
