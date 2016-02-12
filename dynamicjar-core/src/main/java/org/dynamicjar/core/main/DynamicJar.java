package org.dynamicjar.core.main;

import org.apache.commons.collections4.CollectionUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dynamic.core.api.DependencyResolver;
import org.dynamic.core.api.exception.DependencyResolutionException;
import org.dynamic.core.api.model.DependencyTreeNode;
import org.dynamic.core.api.util.LambdaExceptionUtil;
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
import java.util.HashSet;
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

    public static void loadDependencies(final Class mainClass, final String groupId, final String artifactId)
        throws IOException, XmlPullParserException {

        String path = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";

        InputStream pomInputStream = mainClass.getResourceAsStream(path);

        try {
            Set<DependencyTreeNode> dependencies = new HashSet<>();
            Set<Class<? extends DependencyResolver>> dependencyResolvers = getDependencyResolvers();
            if (CollectionUtils.isEmpty(dependencyResolvers)) {
                throw new DependencyResolutionException(
                    "Failed to find implementations of " + DependencyResolver.class.getName());
            }
            dependencyResolvers.stream()
                .forEach(LambdaExceptionUtil.rethrowConsumer(dependencyResolver -> {
                    try {
                        DependencyTreeNode dependencyRoot =
                            dependencyResolver.newInstance().getDependencyFiles(pomInputStream);
                        if (dependencyRoot != null) {
                            dependencies.add(dependencyRoot);
                        }
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new DependencyResolutionException(e);
                    }
                }));
            logger.debug(dependencies.toString());
        } catch (DependencyResolutionException e) {
            logger.error("Failed to resolve dependencies", e);
        }
        if (pomInputStream != null) {
            pomInputStream.close();
        }
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
            "Scanning classpath for implementations of [" + DependencyResolver.class.getName()
            + "] took " + (System.currentTimeMillis() - before) + "ms.");
        return dependencyResolvers;
    }


    public static void addJar(final String jar) throws IOException {
        File f = new File(jar);
        addJar(f);
    }

    public static void addJar(final File jar) throws IOException {
        addJar(jar.toURI().toURL());
    }

    public static void addJar(final URL jar) throws IOException {
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
