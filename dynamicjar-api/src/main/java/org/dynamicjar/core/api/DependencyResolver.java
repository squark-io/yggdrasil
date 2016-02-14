package org.dynamicjar.core.api;

import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.model.DependencyTreeNode;

import java.io.InputStream;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-12.
 * Copyright 2016
 */
public interface DependencyResolver {

    DependencyTreeNode getDependencyFiles(InputStream dependencyDefinitions)
    throws DependencyResolutionException;

    InputStream getDependencyDescriberFor(Class clazz, String groupId, String artifactId);
}
