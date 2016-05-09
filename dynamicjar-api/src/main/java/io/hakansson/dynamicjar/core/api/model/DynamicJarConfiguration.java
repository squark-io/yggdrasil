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
    private String mainClass;
    private boolean loadTransitiveProvidedDependencies;
    private Set<ProviderConfiguration> providerConfigurations;
    private LoggerConfiguration loggerConfiguration;
    private String classesJar;

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

    public boolean isLoadTransitiveProvidedDependencies() {
        return loadTransitiveProvidedDependencies;
    }

    public void setLoadTransitiveProvidedDependencies(boolean loadTransitiveProvidedDependencies) {
        this.loadTransitiveProvidedDependencies = loadTransitiveProvidedDependencies;
    }

    public LoggerConfiguration getLoggerConfiguration() {
        return loggerConfiguration;
    }

    public void setLoggerConfiguration(LoggerConfiguration loggerConfiguration) {
        this.loggerConfiguration = loggerConfiguration;
    }

    public String getClassesJar() {
        return classesJar;
    }

    public void setClassesJar(String classesJar) {
        this.classesJar = classesJar;
    }

    public class LoggerConfiguration {
        private String configFile;
        private String defaultLogLevel;

        public String getConfigFile() {
            return configFile;
        }

        public void setConfigFile(String configFile) {
            this.configFile = configFile;
        }

        public String getDefaultLogLevel() {
            return defaultLogLevel;
        }

        public void setDefaultLogLevel(String defaultLogLevel) {
            this.defaultLogLevel = defaultLogLevel;
        }
    }
}
