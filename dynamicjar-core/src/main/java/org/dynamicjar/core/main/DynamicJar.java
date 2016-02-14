package org.dynamicjar.core.main;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dynamic.core.api.DependencyResolver;
import org.dynamic.core.api.exception.DependencyResolutionException;
import org.dynamic.core.api.model.DependencyTreeNode;
import org.dynamic.core.api.util.LambdaExceptionUtil;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
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
 * *** AUTOTRADE ***
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

    public static void loadDependencies(final Class mainClass, final String groupId,
        final String artifactId) throws IOException, XmlPullParserException {

        Set<DependencyTreeNode> dependencies = new HashSet<>();
        try {
            Set<Class<? extends DependencyResolver>> dependencyResolvers = getDependencyResolvers();
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
                            .getDependencyDescriberFor(mainClass, groupId, artifactId);
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

            loadJars(dependencies, mainClass);

        } catch (DependencyResolutionException e) {
            logger.error("Failed to resolve dependencies", e);
        }


    }

    private static void loadJars(final Set<DependencyTreeNode> dependencies, Class forClass) throws IOException {
        Map<String, String> loadedJars = new HashMap<>();
        List<DependencyTreeNode> flatDependencies = getFlatDependencies(dependencies);
        for (DependencyTreeNode dependency : flatDependencies) {
            if (!StringUtils.equals(dependency.getScope(), JavaScopes.PROVIDED)) {
                logger.debug(
                    "Found dependency " + dependency.buildIdentifierString() + " not of scope " +
                    JavaScopes.PROVIDED + ". Skipping.");
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
            addJar(dependency.getFile(), forClass);
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

    private static Set<Class<? extends DependencyResolver>> getDependencyResolvers() {
        Long before = System.currentTimeMillis();
        Reflections reflections = new Reflections(
            new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(new SubTypesScanner())
                .filterInputsBy(new FilterBuilder().include(".*\\.class")));
        Set<Class<? extends DependencyResolver>> dependencyResolvers =
            reflections.getSubTypesOf(DependencyResolver.class);
        logger.debug(
            "Scanning classpath for implementations of [" + DependencyResolver.class.getName() +
            "] took " + (System.currentTimeMillis() - before) + "ms.");
        return dependencyResolvers;
    }

    public static void addJar(final String jar, Class forClass) throws IOException {
        File file = new File(jar);
        addJar(file, forClass);
    }

    public static void addJar(final File jar, Class forClass) throws IOException {
        addJar(jar.toURI().toURL(), forClass);
    }

    public static void addJar(final URL jar, Class forClass) throws IOException {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> urlClassLoaderClass = URLClassLoader.class;
        try {
            Method method = urlClassLoaderClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(systemClassLoader, jar);
        } catch (Throwable t) {
            logger.error("Failed to load JAR", t);
            throw new IOException(t);
        }

    }


}
