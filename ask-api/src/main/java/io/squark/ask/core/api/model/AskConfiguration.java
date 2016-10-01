package io.squark.ask.core.api.model;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-02-23.
 * Copyright 2016
 */
public class AskConfiguration implements Serializable {
    private Set<AskDependency> dependencies;
    private String askVersion;
    private String mainClass;
    private boolean loadTransitiveProvidedDependencies;
    private Set<ProviderConfiguration> providerConfigurations;
    private String classesJar;

    public Set<AskDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<AskDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getAskVersion() {
        return askVersion;
    }

    public void setAskVersion(String askVersion) {
        this.askVersion = askVersion;
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
