package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-09.
 * Copyright 2016
 */
public interface FrameworkProvider {
    void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException;
}
