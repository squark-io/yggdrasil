package org.dynamicjar.core.api.model;

import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class DynamicJarConfiguration {
    Set<DynamicJarDependency> dependencies;

    public Set<DynamicJarDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<DynamicJarDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
