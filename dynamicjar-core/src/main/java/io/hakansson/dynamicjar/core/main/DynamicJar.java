package io.hakansson.dynamicjar.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.FrameworkProviderService;
import io.hakansson.dynamicjar.core.api.exception.*;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.core.api.util.LibHelper;
import io.hakansson.dynamicjar.core.api.logging.LogHelper;
import io.hakansson.dynamicjar.core.api.util.ReflectionUtil;
import io.hakansson.dynamicjar.logging.api.InternalLogger;
import io.hakansson.dynamicjar.logging.api.LogLevel;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarURLStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-04.
 * Copyright 2016
 */
public final class DynamicJar {

    private static InternalLogger logger = InternalLogger.getLogger(DynamicJar.class);

    static { //runs when the main class is loaded.
        //This is necessary for some modules when not using Log4J2:
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    @SuppressWarnings("unused")
    static void internalMain(String[] args) {
        logger.log(LogLevel.INFO, "Initiating DynamicJar");
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            LogHelper.initiateLogging(null, classLoader, null, false);
            DynamicJarConfiguration configuration = getConfiguration();
            NestedJarClassLoader isolatedClassLoader = initiate(classLoader, configuration);
            loadMainClass(isolatedClassLoader, configuration, args);
        } catch (DynamicJarException e) {
            logger.log(LogLevel.ERROR, e);
        }
    }

    static DynamicJarConfiguration getConfiguration() throws PropertyLoadException {

        InputStream inputStream;
        inputStream = DynamicJar.class.getResourceAsStream(Constants.YAML_PROPERTIES_FILE);
        DynamicJarConfiguration dynamicJarConfiguration = null;
        if (inputStream == null) {
            inputStream = DynamicJar.class.getResourceAsStream(Constants.JSON_PROPERTIES_FILE);
            if (inputStream != null) {
                logger.log(LogLevel.INFO, "Found JSON configuration");
                Gson gson = new Gson();
                try {
                    dynamicJarConfiguration = gson.fromJson(new InputStreamReader(inputStream), DynamicJarConfiguration.class);
                } catch (JsonParseException e) {
                    throw new PropertyLoadException(e);
                }
            }
        } else {
            logger.log(LogLevel.INFO, "Found YAML configuration");
            Yaml yaml = new Yaml();
            try {
                dynamicJarConfiguration = yaml.loadAs(inputStream, DynamicJarConfiguration.class);
            } catch (YAMLException e) {
                throw new PropertyLoadException(e);
            }
        }
        if (inputStream == null) {
            throw new PropertyLoadException(
                    "Failed to find " + Constants.YAML_PROPERTIES_FILE + " or " + Constants.JSON_PROPERTIES_FILE);
        } else if (dynamicJarConfiguration == null) {
            throw new PropertyLoadException("Unknown error. Failed to load properties");
        }

        return dynamicJarConfiguration;

    }

    private static void loadMainClass(ClassLoader forClassLoader, DynamicJarConfiguration configuration, String[] args) throws
            MainClassLoadException
    {
        //Load main class:
        if (StringUtils.isNotEmpty(configuration.getMainClass())) {
            logger.log(LogLevel.DEBUG, "Loading main class " + configuration.getMainClass());
            try {
                Class<?> mainClass = Class.forName(configuration.getMainClass(), true, forClassLoader);
                ReflectionUtil.invokeMethod("main", mainClass, null, new Object[]{args}, null);
                logger.log(LogLevel.DEBUG, "Main class loaded");
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new MainClassLoadException(e);
            }
        }
    }

    public static NestedJarClassLoader initiate(ClassLoader classLoader, DynamicJarConfiguration configuration) throws
            NestedJarClassloaderException
    {
        if (!(classLoader instanceof NestedJarClassLoader)) {
            classLoader = new NestedJarClassLoader(new URL[]{}, classLoader, true);
        }

        try {
            Class<?> dynamicJarClass = Class.forName(DynamicJar.class.getName(), true, classLoader);
            return (NestedJarClassLoader) ReflectionUtil.invokeMethod("initiate", dynamicJarClass, null,
                    new Object[]{ConfigurationSerializer.serializeConfig(configuration), classLoader},
                    new Class[]{byte[].class, Object.class});
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            throw new NestedJarClassloaderException(e);
        }
    }

    @SuppressWarnings("unused")
    private static NestedJarClassLoader initiate(byte[] configurationBytes, Object helperClassLoader) throws DynamicJarException {

        Thread.currentThread().setContextClassLoader((ClassLoader) helperClassLoader);
        DynamicJarConfiguration configuration = ConfigurationSerializer.deserializeConfig(configurationBytes);

        URL libs[] = LibHelper.getLibs(Constants.LIB_PATH);
        URL classesJar = getClassesJar(configuration);

        NestedJarClassLoader isolatedClassLoader = new NestedJarClassLoader(libs, null, true);
        try {
            ReflectionUtil.invokeMethod("initiateLogging", Class.forName(LogHelper.class.getName(), true, isolatedClassLoader),
                    null, new Object[]{configurationBytes, isolatedClassLoader, classesJar},
                    new Class[]{byte[].class, Object.class, URL.class});
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            throw new DependencyResolutionException(e);
        }

        Set<String> loadedLibs = new HashSet<>();
        for (URL url : libs) {
            loadedLibs.add(FilenameUtils.getName(url.getFile()));
        }

        RemoteDependencyLoader.loadDependencies(isolatedClassLoader, (NestedJarClassLoader) helperClassLoader, configuration,
                loadedLibs);

        FrameworkProviderService.loadProviders(isolatedClassLoader, configuration);
        logger.log(LogLevel.INFO, "DynamicJar initiated");
        return isolatedClassLoader;
    }

    private static URL getClassesJar(DynamicJarConfiguration configuration) throws DependencyResolutionException {
        try {
            File ownFile = new File(DynamicJar.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JarFile ownJar = new JarFile(ownFile);
            Enumeration<JarEntry> entries = ownJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(configuration.getClassesJar())) {
                    return new URL(null, "jar:" + ownFile.toURI().toString() + "!/" + entry.getName(),
                            new NestedJarURLStreamHandler());
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new DependencyResolutionException(e);
        }
        return null;
    }

    public static NestedJarClassLoader initiate(Class forClass, DynamicJarConfiguration configuration) throws DynamicJarException
    {
        return initiate(forClass.getClassLoader(), configuration);
    }

}
