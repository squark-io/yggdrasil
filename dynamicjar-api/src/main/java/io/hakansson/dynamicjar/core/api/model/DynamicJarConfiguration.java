package io.hakansson.dynamicjar.core.api.model;

import java.io.Serializable;
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
    private String mainClass;
    private boolean loadTransitiveProvidedDependencies;
    private Set<ProviderConfiguration> providerConfigurations;
    private String classesJar;

    public Set<DynamicJarDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<DynamicJarDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getDynamicJarVersion() {
        return dynamicJarVersion;
    }

    public void setDynamicJarVersion(String dynamicJarVersion) {
        this.dynamicJarVersion = dynamicJarVersion;
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

    public boolean isLoadTransitiveProvidedDependencies() {
        return loadTransitiveProvidedDependencies;
    }

    public void setLoadTransitiveProvidedDependencies(boolean loadTransitiveProvidedDependencies) {
        this.loadTransitiveProvidedDependencies = loadTransitiveProvidedDependencies;
    }

    public String getClassesJar() {
        return classesJar;
    }

    public void setClassesJar(String classesJar) {
        this.classesJar = classesJar;
    }

}
