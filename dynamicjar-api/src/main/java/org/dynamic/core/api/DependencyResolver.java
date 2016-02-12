package org.dynamic.core.api;

import org.dynamic.core.api.exception.DependencyResolutionException;
import org.dynamic.core.api.model.DependencyTreeNode;

import java.io.InputStream;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-12.
 * Copyright 2016
 */
public interface DependencyResolver {

    DependencyTreeNode getDependencyFiles(InputStream dependencyDefinitions)
    throws DependencyResolutionException;
}
