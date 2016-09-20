package io.hakansson.dynamicjar.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.FrameworkProviderService;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.MainClassLoadException;
import io.hakansson.dynamicjar.core.api.exception.PropertyLoadException;
import io.hakansson.dynamicjar.core.api.logging.LogHelper;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.core.api.util.LibHelper;
import io.hakansson.dynamicjar.core.api.util.ReflectionUtil;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarURLStreamHandler;
import io.hakansson.dynamicjar.nestedjarclassloader.exception.NestedJarClassLoaderException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private static Logger logger = InternalLoggerBinder.getLogger(DynamicJar.class);

    static { //runs when the main class is loaded.
        //This is necessary for some modules when not using Log4J2:
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    @SuppressWarnings("unused")
    public static void internalMain(String[] args) {
        logger.info("Initiating DynamicJar");
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            LogHelper.initiateLogging(null, classLoader, null, false);
            DynamicJarConfiguration configuration = getConfiguration();
            NestedJarClassLoader isolatedClassLoader = initiate(classLoader, configuration);
            loadMainClass(isolatedClassLoader, configuration, args);
        } catch (DynamicJarException | NestedJarClassLoaderException e) {
            logger.error(null, e);
        }
    }

    static DynamicJarConfiguration getConfiguration() throws PropertyLoadException {

        InputStream inputStream;
        inputStream = DynamicJar.class.getResourceAsStream(Constants.YAML_PROPERTIES_FILE);
        DynamicJarConfiguration dynamicJarConfiguration = null;
        if (inputStream == null) {
            inputStream = DynamicJar.class.getResourceAsStream(Constants.JSON_PROPERTIES_FILE);
            if (inputStream != null) {
                logger.info("Found JSON configuration");
                Gson gson = new Gson();
                try {
                    dynamicJarConfiguration = gson.fromJson(new InputStreamReader(inputStream), DynamicJarConfiguration.class);
                } catch (JsonParseException e) {
                    throw new PropertyLoadException(e);
                }
            }
        } else {
            logger.info("Found YAML configuration");
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
            logger.debug("Loading main class " + configuration.getMainClass());
            try {
                ReflectionUtil.invokeMethod("main", configuration.getMainClass(), null, new Object[]{args}, null, forClassLoader,
                        null);
                logger.debug("Main class loaded");
            } catch (Throwable e) {
                throw new MainClassLoadException(e);
            }
        }
    }

    public static NestedJarClassLoader initiate(ClassLoader classLoader, DynamicJarConfiguration configuration) throws
            NestedJarClassLoaderException
    {
        if (!(classLoader instanceof NestedJarClassLoader)) {
            classLoader = new NestedJarClassLoader(classLoader);
        }

        try {
            return ReflectionUtil.invokeMethod("initiate", DynamicJar.class.getName(), null,
                    new Object[]{ConfigurationSerializer.serializeConfig(configuration), classLoader},
                    new Class[]{byte[].class, Object.class}, classLoader, NestedJarClassLoader.class);
        } catch (Throwable e) {
            throw new NestedJarClassLoaderException(e);
        }
    }

    @SuppressWarnings("unused")
    private static NestedJarClassLoader initiate(byte[] configurationBytes, Object helperClassLoader) throws DynamicJarException {

        Thread.currentThread().setContextClassLoader((ClassLoader) helperClassLoader);
        DynamicJarConfiguration configuration = ConfigurationSerializer.deserializeConfig(configurationBytes);

        URL libs[] = LibHelper.getLibs(DynamicJar.class, Constants.LIB_PATH);
        URL classesJar = getClassesJar(configuration);

        NestedJarClassLoader isolatedClassLoader;
        try {
            isolatedClassLoader = new NestedJarClassLoader(null);
            isolatedClassLoader.addURLs(libs);
        } catch (IOException e) {
            throw new DynamicJarException(e);
        }
        try {
            //Initiate logging again in case we found logging module, but do it in the isolated classloader.
            ReflectionUtil.invokeMethod("initiateLoggingWithConfigAsBytes", LogHelper.class.getName(), null,
                    new Object[]{configurationBytes, isolatedClassLoader, classesJar},
                    new Class[]{byte[].class, Object.class, URL.class}, isolatedClassLoader, null);
        } catch (Throwable e) {
            throw new DynamicJarException(e);
        }

        Set<String> loadedLibs = new HashSet<>();
        for (URL url : libs) {
            loadedLibs.add(FilenameUtils.getName(url.getFile()));
        }

        RemoteDependencyLoader.loadDependencies(isolatedClassLoader, (NestedJarClassLoader) helperClassLoader, configuration,
                loadedLibs);

        try {
            //Initiate logging again in case we found logging module, but do it in the isolated classloader.
            ReflectionUtil.invokeMethod("initiateLoggingWithConfigAsBytes", LogHelper.class.getName(), null,
                    new Object[]{configurationBytes, isolatedClassLoader, classesJar, false},
                    new Class[]{byte[].class, Object.class, URL.class, boolean.class}, isolatedClassLoader, null);
        } catch (Throwable e) {
            throw new DynamicJarException(e);
        }

        FrameworkProviderService.loadProviders(isolatedClassLoader, configuration);
        logger.info("DynamicJar initiated");
        return isolatedClassLoader;
    }

    private static URL getClassesJar(DynamicJarConfiguration configuration) throws DynamicJarException {
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
            throw new DynamicJarException(e);
        }
        return null;
    }

}
