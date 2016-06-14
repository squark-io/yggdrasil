package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;

import java.util.Set;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-12.
 * Copyright 2016
 */
public interface DependencyResolutionProvider {

    Set<DynamicJarDependency> resolveDependencies(Set<DynamicJarDependency> dependencies,
        boolean loadTransitiveProvidedDependencies) throws DependencyResolutionException;

}
