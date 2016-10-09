/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.core.api.model;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

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
