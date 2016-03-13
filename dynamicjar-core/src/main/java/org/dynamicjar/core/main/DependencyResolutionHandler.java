package org.dynamicjar.core.main;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dynamicjar.core.api.DependencyResolutionProvider;
import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.exception.PropertyLoadException;
import org.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.dynamicjar.core.api.model.DynamicJarDependency;
import org.dynamicjar.core.api.util.LambdaExceptionUtil;
import org.dynamicjar.core.api.util.Scopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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

    private static void loadJars(final Set<DynamicJarDependency> dependencies,
        ClassLoader classLoader)
        throws IOException {
        Map<String, String> loadedJars = new HashMap<>();
        List<DynamicJarDependency> flatDependencies = getFlatDependencies(dependencies);
        for (DynamicJarDependency dependency : flatDependencies) {
            if (!StringUtils.equals(dependency.getScope(), Scopes.PROVIDED)) {
                logger.debug("Found dependency " + dependency.toShortString() + " of scope " +
                             dependency.getScope() + ". Skipping.");
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
            addJar(dependency.getFile(), classLoader);
            loadedJars.put(dependency.toShortStringWithoutVersion(), dependency.getVersion());
        }
    }

    private static void addJar(final URL jar, ClassLoader classLoader) throws IOException {
        Class<?> urlClassLoaderClass = URLClassLoader.class;
        try {
            Method method = urlClassLoaderClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, jar);
        } catch (Throwable t) {
            logger.error("Failed to load JAR", t);
            throw new IOException(t);
        }
    }

    static void loadDependencies(ClassLoader classLoader,
        DynamicJarConfiguration dynamicJarConfiguration)
        throws PropertyLoadException, DependencyResolutionException {

        Collection<Class<? extends DependencyResolutionProvider>> dependencyResolvers =
            DependencyResolutionProviderFactory.getDependencyResolvers(dynamicJarConfiguration);
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

    private static void addJar(final File jar, ClassLoader classLoader) throws IOException {
        addJar(jar.toURI().toURL(), classLoader);
    }
}
