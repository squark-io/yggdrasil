package io.hakansson.dynamicjar.core.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class DynamicJarConfiguration {
    private Set<DynamicJarDependency> dependencies;
    private String dynamicJarVersion;
    private String dependencyResolutionProviderClass;
    private String mainClass;

    public Set<DynamicJarDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<DynamicJarDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(DynamicJarDependency dependency) {
        if (dependencies == null) {
            dependencies = new HashSet<>();
        }
        dependencies.add(dependency);
    }

    public String getDynamicJarVersion() {
        return dynamicJarVersion;
    }

    public void setDynamicJarVersion(String dynamicJarVersion) {
        this.dynamicJarVersion = dynamicJarVersion;
    }

    public String getDependencyResolutionProviderClass() {
        return dependencyResolutionProviderClass;
    }

    public void setDependencyResolutionProviderClass(String dependencyResolutionProviderClass) {
        this.dependencyResolutionProviderClass = dependencyResolutionProviderClass;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
}
