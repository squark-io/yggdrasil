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
package io.squark.yggdrasil.core.main;

import io.squark.nestedjarclassloader.NestedJarClassLoader;
import io.squark.yggdrasil.core.api.DependencyResolutionProvider;
import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.api.exception.PropertyLoadException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.core.api.model.YggdrasilDependency;
import io.squark.yggdrasil.core.api.util.Scopes;
import io.squark.yggdrasil.core.main.factory.DependencyResolutionProviderFactory;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoteDependencyLoader {

    private static final Logger logger = InternalLoggerBinder.getLogger(RemoteDependencyLoader.class);

    private RemoteDependencyLoader() {
    }

    private static boolean anyParentIsProvided(YggdrasilDependency child) {
        return child.getParent().isPresent() &&
               (Scopes.PROVIDED.equals(child.getParent().get().getScope()) || anyParentIsProvided(child.getParent().get()));
    }

    private static void loadJars(final Set<YggdrasilDependency> dependencies, NestedJarClassLoader classLoader,
        boolean includeTransitive, Set<String> exclusions) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading the following dependencies: " + dependencies);
        }
        Map<String, String> loadedJars = new HashMap<>();
        List<YggdrasilDependency> flatDependencies = getFlatDependencies(dependencies, includeTransitive);
        filterDependencies(flatDependencies, exclusions);
        for (YggdrasilDependency dependency : flatDependencies) {
            if ((!StringUtils.equals(dependency.getScope(), Scopes.PROVIDED) && !anyParentIsProvided(dependency)) ||
                dependency.getOptional()) {
                logger.debug(
                    "Found dependency " + dependency.toShortString() + " of scope " + dependency.getScope() + " and optional=" +
                    dependency.getOptional() + ". Skipping.");
                continue;
            }
            String identifier = dependency.toShortStringWithoutVersion();
            String loadedVersion = loadedJars.get(identifier);
            if (dependency.getFile() == null) {
                logger.warn("No jar found for " + dependency.toShortString());
                continue;
            }
            if (loadedVersion != null) {
                if (!StringUtils.equals(loadedVersion, dependency.getVersion())) {
                    logger.warn("Dependency " + identifier + " exists in at least two versions: {" + loadedVersion + ", " +
                                dependency.getVersion() + "}. Only first found will be loaded.");
                }
                continue;
            }
            logger.debug("Loading dependency " + dependency.toShortString() + " of scope " + dependency.getScope());
            boolean added = addJar(dependency.getFile(), classLoader);
            if (added) {
                loadedJars.put(dependency.toShortStringWithoutVersion(), dependency.getVersion());
            }
        }
    }

    private static boolean addJar(final URL jar, NestedJarClassLoader classLoader) throws IOException {
        try {
            File jarFile = new File(jar.toURI());
            if (!jarFile.exists()) {
                logger.warn(Marker.ANY_MARKER, "Cannot find JAR " + jar + ". Skipping.");
                return false;
            } else if (!jarFile.canRead()) {
                logger.warn(Marker.ANY_MARKER, "Cannot read JAR " + jar + ". No read access? Skipping.");
                return false;
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        try {
            classLoader.addURLs(jar);
            return true;
        } catch (Throwable t) {
            logger.error("Failed to load JAR", t);
            throw new IOException(t);
        }
    }

    static void loadDependencies(NestedJarClassLoader classLoader, NestedJarClassLoader helperClassLoader,
        YggdrasilConfiguration yggdrasilConfiguration, Set<String> exclusions)
        throws PropertyLoadException, DependencyResolutionException {

        Collection<DependencyResolutionProvider> dependencyResolvers =
            DependencyResolutionProviderFactory.getDependencyResolvers(helperClassLoader);
        if (CollectionUtils.isEmpty(dependencyResolvers)) {
            logger.warn("Failed to find implementations of " + DependencyResolutionProvider.class.getName());
        }
        final Set<YggdrasilDependency> dependencies = yggdrasilConfiguration.getDependencies();
        Set<YggdrasilDependency> resolvedDependencies = new HashSet<>();
        if (dependencies != null) {
            for (DependencyResolutionProvider provider : dependencyResolvers) {
                resolvedDependencies.addAll(
                    provider.resolveDependencies(dependencies, yggdrasilConfiguration.isLoadTransitiveProvidedDependencies()));
            }
        }
        try {
            loadJars(resolvedDependencies, classLoader, yggdrasilConfiguration.isLoadTransitiveProvidedDependencies(),
                exclusions);
        } catch (IOException e) {
            throw new DependencyResolutionException("Failed to resolve dependencies", e);
        }
    }

    private static void filterDependencies(List<YggdrasilDependency> dependencies, Set<String> exclusions) {
        Iterator<YggdrasilDependency> dependencyIterator = dependencies.iterator();
        while (dependencyIterator.hasNext()) {
            YggdrasilDependency dependency = dependencyIterator.next();
            String fileName = dependency.getFile().getName();
            if (exclusions.contains(fileName)) {
                logger.debug(
                    "A file with the name " + fileName + " was loaded from libs, but is also found in remote dependency " +
                    dependency.toShortString() + ". Assuming same and skipping.");
                dependencyIterator.remove();
            }
        }
    }

    private static List<YggdrasilDependency> getFlatDependencies(final Collection<YggdrasilDependency> dependencies,
        boolean includeTransitive) {
        return getFlatDependencies(dependencies, includeTransitive, 0);
    }

    private static List<YggdrasilDependency> getFlatDependencies(final Collection<YggdrasilDependency> dependencies,
        boolean includeTransitive, int depth) {
        List<YggdrasilDependency> jars = new ArrayList<>();
        if (dependencies != null) {
            for (YggdrasilDependency dependency : dependencies) {
                if (Scopes.COMPILE.equals(dependency.getScope()) ||
                    ((Scopes.PROVIDED.equals(dependency.getScope())) && (includeTransitive || depth == 0))) {
                    jars.add(dependency);
                }
                jars.addAll(getFlatDependencies(dependency.getChildDependencies(), includeTransitive, depth + 1));
            }
        }
        return jars;
    }

    private static boolean addJar(final File jar, NestedJarClassLoader classLoader) throws IOException {
        return addJar(jar.toURI().toURL(), classLoader);
    }
}
