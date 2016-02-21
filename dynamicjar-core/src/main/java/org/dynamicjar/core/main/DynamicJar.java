package org.dynamicjar.core.main;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dynamicjar.core.api.DependencyResolver;
import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.model.DependencyTreeNode;
import org.dynamicjar.core.api.util.LambdaExceptionUtil;
import org.dynamicjar.core.api.util.Scopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-04.
 * Copyright 2016
 */
public final class DynamicJar {

    private static Logger logger = LoggerFactory.getLogger(DynamicJar.class);

    private DynamicJar() {
        //Disallow instantiation
    }

    public static void loadDependencies(final String groupId, final String artifactId)
        throws DependencyResolutionException {
        loadDependencies(groupId, artifactId, ClassLoader.getSystemClassLoader());
    }

    public static void loadDependencies(final String groupId, final String artifactId,
        final Class forClass) throws DependencyResolutionException {
        loadDependencies(groupId, artifactId, forClass.getClassLoader());
    }

    public static void loadDependencies(final String groupId, final String artifactId,
        final ClassLoader classLoader) throws DependencyResolutionException {

        Set<DependencyTreeNode> dependencies = new HashSet<>();
        try {
            Collection<Class<? extends DependencyResolver>> dependencyResolvers =
                DependencyResolverFactory.getDependencyResolvers();
            if (CollectionUtils.isEmpty(dependencyResolvers)) {
                throw new DependencyResolutionException(
                    "Failed to find implementations of " + DependencyResolver.class.getName());
            }
            dependencyResolvers.stream()
                .forEach(LambdaExceptionUtil.rethrowConsumer(dependencyResolver -> {
                    try {
                        DependencyResolver dependencyResolverInstance =
                            dependencyResolver.newInstance();
                        InputStream dependencyDescriber = dependencyResolverInstance
                            .getDependencyDescriberFor(groupId, artifactId);
                        DependencyTreeNode dependencyRoot =
                            dependencyResolverInstance.getDependencyFiles(dependencyDescriber);
                        if (dependencyRoot != null) {
                            dependencies.add(dependencyRoot);
                        }
                        if (dependencyDescriber != null) {
                            dependencyDescriber.close();
                        }
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new DependencyResolutionException(e);
                    }
                }));
            logger.debug(dependencies.toString());

            loadJars(dependencies, classLoader);

        } catch (IOException e) {
            throw new DependencyResolutionException("Failed to resolve dependencies", e);
        }

    }

    private static void loadJars(final Set<DependencyTreeNode> dependencies,
        ClassLoader classLoader) throws IOException {
        Map<String, String> loadedJars = new HashMap<>();
        List<DependencyTreeNode> flatDependencies = getFlatDependencies(dependencies);
        for (DependencyTreeNode dependency : flatDependencies) {
            if (!StringUtils.equals(dependency.getScope(), Scopes.PROVIDED)) {
                logger.debug(
                    "Found dependency " + dependency.buildIdentifierString() + " not of scope " +
                    Scopes.PROVIDED + ". Skipping.");
                continue;
            }
            String identifier = dependency.buildIdentifierStringWithoutVersion();
            String loadedVersion = loadedJars.get(identifier);
            if (dependency.getFile() == null) {
                logger.warn("No jar found for " + dependency.buildIdentifierString());
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
            logger.debug("Loading dependency " + dependency.buildIdentifierString());
            addJar(dependency.getFile(), classLoader);
            loadedJars
                .put(dependency.buildIdentifierStringWithoutVersion(), dependency.getVersion());
        }
    }

    private static List<DependencyTreeNode> getFlatDependencies(
        final Collection<DependencyTreeNode> dependencies) {
        List<DependencyTreeNode> jars = new ArrayList<>();
        for (DependencyTreeNode dependencyTreeNode : dependencies) {
            jars.add(dependencyTreeNode);
            jars.addAll(getFlatDependencies(dependencyTreeNode.getChildDependencies().values()));
        }
        return jars;
    }

    public static void addJar(final String jar, ClassLoader classLoader) throws IOException {
        File file = new File(jar);
        addJar(file, classLoader);
    }

    public static void addJar(final File jar, ClassLoader classLoader) throws IOException {
        addJar(jar.toURI().toURL(), classLoader);
    }

    public static void addJar(final URL jar, ClassLoader classLoader) throws IOException {
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


}
