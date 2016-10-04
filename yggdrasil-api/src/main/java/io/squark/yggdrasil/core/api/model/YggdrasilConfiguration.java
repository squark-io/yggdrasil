package io.squark.yggdrasil.core.api.model;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class YggdrasilConfiguration implements Serializable {
    private Set<YggdrasilDependency> dependencies;
    private String yggdrasilVersion;
    private String mainClass;
    private boolean loadTransitiveProvidedDependencies;
    private Set<ProviderConfiguration> providerConfigurations;
    private String classesJar;

    public Set<YggdrasilDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<YggdrasilDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getYggdrasilVersion() {
        return yggdrasilVersion;
    }

    public void setYggdrasilVersion(String yggdrasilVersion) {
        this.yggdrasilVersion = yggdrasilVersion;
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
