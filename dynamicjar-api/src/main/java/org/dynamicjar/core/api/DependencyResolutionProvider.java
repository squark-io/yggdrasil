package org.dynamicjar.core.api;

import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.model.DynamicJarDependency;
import org.dynamicjar.core.api.util.LambdaExceptionUtil;

import java.io.InputStream;
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

    DynamicJarDependency resolveDependencies(InputStream dependencyDefinitions)
    throws DependencyResolutionException;

    InputStream getDependencyDescriberFor(String groupId, String artifactId);

    DynamicJarDependency resolveDependency(DynamicJarDependency dependency)
        throws DependencyResolutionException;

    default Set<DynamicJarDependency> resolveDependencies(Set<DynamicJarDependency> dependencies) {
        Set<DynamicJarDependency> resolvedSet = new HashSet<>();
        dependencies.parallelStream().forEach(LambdaExceptionUtil.rethrowConsumer(dynamicJarDependency -> {
            resolvedSet.add(resolveDependency(dynamicJarDependency));
        }));
        return resolvedSet;
    }
}
