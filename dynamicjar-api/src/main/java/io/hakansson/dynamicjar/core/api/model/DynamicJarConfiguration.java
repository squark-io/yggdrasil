package io.hakansson.dynamicjar.core.api.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class DynamicJarConfiguration implements Serializable {
    private Set<DynamicJarDependency> dependencies;
    private String dynamicJarVersion;
    private String dependencyResolutionProviderClass;
    private String mainClass;
    private Set<ProviderConfiguration> providerConfigurations;

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

    public Optional<Set<ProviderConfiguration>> getProviderConfigurations() {
        return Optional.ofNullable(providerConfigurations);
    }

    public void setProviderConfigurations(Set<ProviderConfiguration> providerConfigurations) {
        this.providerConfigurations = providerConfigurations;
    }

    public void addProviderConfiguration(ProviderConfiguration providerConfiguration) {
        if (this.providerConfigurations == null) {
            this.providerConfigurations = new HashSet<>();
        }
        this.providerConfigurations.add(providerConfiguration);
    }
}
