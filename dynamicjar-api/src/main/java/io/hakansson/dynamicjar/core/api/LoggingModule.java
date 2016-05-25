package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public interface LoggingModule {
    void initialize(@Nullable DynamicJarConfiguration configuration, @Nullable ClassLoader classLoader, @Nullable URL jarWithConfig)
    throws DynamicJarException;
}
