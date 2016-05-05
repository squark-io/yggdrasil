package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;

import java.util.HashSet;
import java.util.Set;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-12.
 * Copyright 2016
 */
public interface DependencyResolutionProvider {

    DynamicJarDependency resolveDependency(DynamicJarDependency dependency,
        boolean loadTransitiveProvidedDependencies) throws DependencyResolutionException;

    default Set<DynamicJarDependency> resolveDependencies(Set<DynamicJarDependency> dependencies,
        boolean loadTransitiveProvidedDependencies)
        throws DependencyResolutionException {
        Set<DynamicJarDependency> resolvedSet = new HashSet<>();
        for (DynamicJarDependency dynamicJarDependency : dependencies) {
            resolvedSet.add(resolveDependency(dynamicJarDependency, loadTransitiveProvidedDependencies));
        }
        return resolvedSet;
    }

}
