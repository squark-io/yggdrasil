package io.hakansson.dynamicjar.core.main;

import io.hakansson.dynamicjar.core.api.DependencyResolutionProvider;
import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.exception.PropertyLoadException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.model.DynamicJarDependency;
import io.hakansson.dynamicjar.core.api.util.LambdaExceptionUtil;
import io.hakansson.dynamicjar.core.api.util.Scopes;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassloader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public class DependencyResolutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DependencyResolutionHandler.class);

    private DependencyResolutionHandler() {
    }

    private static boolean anyParentIsProvided(DynamicJarDependency child) {
        return child.getParent().isPresent() &&
               (Scopes.PROVIDED.equals(child.getParent().get().getScope()) ||
                anyParentIsProvided(child.getParent().get()));
    }

    private static void loadJars(final Set<DynamicJarDependency> dependencies,
        NestedJarClassloader classLoader) throws IOException {
        Map<String, String> loadedJars = new HashMap<>();
        List<DynamicJarDependency> flatDependencies = getFlatDependencies(dependencies);
        for (DynamicJarDependency dependency : flatDependencies) {
            if ((!StringUtils.equals(dependency.getScope(), Scopes.PROVIDED) &&
                 !anyParentIsProvided(dependency)) || dependency.getOptional()) {
                logger.debug("Found dependency " + dependency.toShortString() + " of scope " +
                             dependency.getScope() + " and optional=" + dependency.getOptional() +
                             ". Skipping.");
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
                    logger.warn("Dependency " + identifier + " exists in at least two versions: {" +
                                loadedVersion + ", " + dependency.getVersion() +
                                "}. Only first found will be loaded.");
                }
                continue;
            }
            logger.debug("Loading dependency " + dependency.toShortString() + " of scope " +
                         dependency.getScope());
            boolean added = addJar(dependency.getFile(), classLoader);
            if (added) {
                loadedJars.put(dependency.toShortStringWithoutVersion(), dependency.getVersion());
            }
        }
    }

    private static boolean addJar(final URL jar, NestedJarClassloader classLoader)
        throws IOException {
        try {
            File jarFile = new File(jar.toURI());
            if (!jarFile.exists()) {
                logger.warn(Marker.ANY_MARKER, "Cannot find JAR " + jar + ". Skipping.");
                return false;
            } else if (!jarFile.canRead()) {
                logger.warn(Marker.ANY_MARKER,
                    "Cannot read JAR " + jar + ". No read access? Skipping.");
                return false;
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        try {
            classLoader.addURL(jar);
            return true;
        } catch (Throwable t) {
            logger.error("Failed to load JAR", t);
            throw new IOException(t);
        }
    }

    static void loadDependencies(NestedJarClassloader classLoader,
        NestedJarClassloader helperClassLoader, DynamicJarConfiguration dynamicJarConfiguration)
        throws PropertyLoadException, DependencyResolutionException {

        Collection<Class<? extends DependencyResolutionProvider>> dependencyResolvers =
            DependencyResolutionProviderFactory
                .getDependencyResolvers(dynamicJarConfiguration, helperClassLoader);
        if (CollectionUtils.isEmpty(dependencyResolvers)) {
            throw new DependencyResolutionException("Failed to find implementations of " +
                                                    DependencyResolutionProvider.class.getName());
        }
        final Set<DynamicJarDependency> dependencies = dynamicJarConfiguration.getDependencies();
        Set<DynamicJarDependency> resolvedDependencies = new HashSet<>();
        dependencyResolvers.stream()
            .forEach(LambdaExceptionUtil.rethrowConsumer(dependencyResolver -> {
                DependencyResolutionProvider dependencyResolutionProviderInstance =
                    dependencyResolver.newInstance();
                resolvedDependencies
                    .addAll(dependencyResolutionProviderInstance.resolveDependencies(dependencies));
            }));
        try {
            loadJars(resolvedDependencies, classLoader);
        } catch (IOException e) {
            throw new DependencyResolutionException("Failed to resolve dependencies", e);
        }

    }

    private static List<DynamicJarDependency> getFlatDependencies(
        final Collection<DynamicJarDependency> dependencies) {
        List<DynamicJarDependency> jars = new ArrayList<>();
        if (dependencies != null) {
            for (DynamicJarDependency dependency : dependencies) {
                jars.add(dependency);
                jars.addAll(getFlatDependencies(dependency.getChildDependencies()));
            }
        }
        return jars;
    }

    private static boolean addJar(final File jar, NestedJarClassloader classLoader)
        throws IOException {
        return addJar(jar.toURI().toURL(), classLoader);
    }
}
