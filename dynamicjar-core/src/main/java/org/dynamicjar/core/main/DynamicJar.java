package org.dynamicjar.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dynamicjar.core.api.DependencyResolver;
import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.exception.PropertyLoadException;
import org.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.dynamicjar.core.api.model.DynamicJarDependency;
import org.dynamicjar.core.api.util.LambdaExceptionUtil;
import org.dynamicjar.core.api.util.Scopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static final String YAML_PROPERTIES_FILE = "/META-INF/dynamicjar.yaml";
    private static final String JSON_PROPERTIES_FILE = "/META-INF/dynamicjar.json";
    private static Logger logger = LoggerFactory.getLogger(DynamicJar.class);

    private DynamicJar() {
        //Disallow instantiation
    }

    public static void loadDependencies()
        throws PropertyLoadException, DependencyResolutionException {
        loadDependencies(ClassLoader.getSystemClassLoader());
    }

    public static void loadDependencies(ClassLoader classLoader)
        throws PropertyLoadException, DependencyResolutionException {
        InputStream inputStream;
        inputStream = DynamicJar.class.getResourceAsStream(YAML_PROPERTIES_FILE);
        DynamicJarConfiguration dynamicJarConfiguration = null;
        if (inputStream == null) {
            inputStream = DynamicJar.class.getResourceAsStream(JSON_PROPERTIES_FILE);
            if (inputStream != null) {
                Gson gson = new Gson();
                try {
                    dynamicJarConfiguration = gson.fromJson(new InputStreamReader(inputStream),
                        DynamicJarConfiguration.class);
                } catch (JsonParseException e) {
                    throw new PropertyLoadException(e);
                }
            }
        } else {
            Yaml yaml = new Yaml();
            try {
                dynamicJarConfiguration = yaml.loadAs(inputStream, DynamicJarConfiguration.class);
            } catch (YAMLException e) {
                throw new PropertyLoadException(e);
            }
        }
        if (inputStream == null) {
            throw new PropertyLoadException(
                "Failed to find " + YAML_PROPERTIES_FILE + " or " + JSON_PROPERTIES_FILE);
        } else if (dynamicJarConfiguration == null) {
            throw new PropertyLoadException("Unknown error. Failed to load properties");
        }
        Collection<Class<? extends DependencyResolver>> dependencyResolvers =
            DependencyResolverFactory.getDependencyResolvers();
        if (CollectionUtils.isEmpty(dependencyResolvers)) {
            throw new DependencyResolutionException(
                "Failed to find implementations of " + DependencyResolver.class.getName());
        }
        final Set<DynamicJarDependency> dependencies = dynamicJarConfiguration.getDependencies();
        Set<DynamicJarDependency> resolvedDependencies = new HashSet<>();
        dependencyResolvers.stream()
            .forEach(LambdaExceptionUtil.rethrowConsumer(dependencyResolver -> {
                DependencyResolver dependencyResolverInstance = dependencyResolver.newInstance();
                resolvedDependencies
                    .addAll(dependencyResolverInstance.resolveDependencies(dependencies));
            }));
        try {
            loadJars(resolvedDependencies, classLoader);
        } catch (IOException e) {
            throw new DependencyResolutionException("Failed to resolve dependencies", e);
        }

    }

    public static void loadDependencies(Class forClass)
        throws PropertyLoadException, DependencyResolutionException {
        loadDependencies(forClass.getClassLoader());
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

        Set<DynamicJarDependency> dependencies = new HashSet<>();
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
                        DynamicJarDependency rootDependency =
                            dependencyResolverInstance.resolveDependencies(dependencyDescriber);
                        if (rootDependency != null) {
                            dependencies.add(rootDependency);
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

    private static void loadJars(final Set<DynamicJarDependency> dependencies,
        ClassLoader classLoader) throws IOException {
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
