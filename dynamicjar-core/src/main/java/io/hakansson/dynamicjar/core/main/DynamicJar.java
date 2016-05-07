package io.hakansson.dynamicjar.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.hakansson.dynamicjar.core.api.FrameworkProviderService;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.MainClassLoadException;
import io.hakansson.dynamicjar.core.api.exception.NestedJarClassloaderException;
import io.hakansson.dynamicjar.core.api.exception.PropertyLoadException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassloader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Scanner;
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

    private static final String YAML_PROPERTIES_FILE = "/META-INF/dynamicjar.yaml";
    private static final String JSON_PROPERTIES_FILE = "/META-INF/dynamicjar.json";
    private static LoggerContext loggerContext;
    private static Logger logger = LoggerFactory.getLogger(DynamicJar.class);
    private static DynamicJarConfiguration configuration;

    static { //runs when the main class is loaded.
        //todo: move to relevant providers
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    private DynamicJar() {
        //private to disallow instantiation
        String logLevelString = System.getProperty("dynamicjar.logLevel", "INFO");
        Level logLevel = Level.getLevel(logLevelString);
        //todo: check if in debug mode or debug flag is passed
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
        final Configuration config = loggerContext.getConfiguration();

        config.getRootLogger().setLevel(logLevel);

    }

    public static void main(String[] args) {
        try {
            DynamicJarConfiguration configuration = getConfiguration();
            NestedJarClassloader helperClassLoader =
                new NestedJarClassloader(getLibs("META-INF/dynamicjar-runtime-lib/"),
                    DynamicJar.class.getClassLoader());
            NestedJarClassloader classloader = initiate(helperClassLoader, configuration);
            loadMainClass(classloader, configuration, args);
        } catch (DynamicJarException e) {
            logger.error(Marker.ANY_MARKER, e);
        }
    }

    static DynamicJarConfiguration getConfiguration() throws PropertyLoadException {
        if (configuration == null) {

            InputStream inputStream;
            inputStream = DynamicJar.class.getResourceAsStream(YAML_PROPERTIES_FILE);
            DynamicJarConfiguration dynamicJarConfiguration = null;
            if (inputStream == null) {
                inputStream = DynamicJar.class.getResourceAsStream(JSON_PROPERTIES_FILE);
                if (inputStream != null) {
                    logger.info("Found JSON configuration");
                    Gson gson = new Gson();
                    try {
                        dynamicJarConfiguration =
                            gson.fromJson(new InputStreamReader(inputStream), DynamicJarConfiguration.class);
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
                    "Failed to find " + YAML_PROPERTIES_FILE + " or " + JSON_PROPERTIES_FILE);
            } else if (dynamicJarConfiguration == null) {
                throw new PropertyLoadException("Unknown error. Failed to load properties");
            }

            DynamicJar.configuration = dynamicJarConfiguration;
        }

        return configuration;
    }

    private static void loadMainClass(ClassLoader forClassLoader, DynamicJarConfiguration configuration, String[] args)
        throws MainClassLoadException {
        //Load main class:
        if (StringUtils.isNotEmpty(configuration.getMainClass())) {
            logger.debug("Loading main class " + configuration.getMainClass());
            try {
                Class<?> mainClass = Class.forName(configuration.getMainClass(), true, forClassLoader);
                Object mainClassInstance = mainClass.newInstance();
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(mainClassInstance, args);
                logger.debug("Main class loaded");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
                throw new MainClassLoadException(e);
            }
        }
    }

    private static URL[] getLibs(String path) throws DynamicJarException {
        try {
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            Set<URL> libs = new HashSet<>();
            File ownFile = new File(DynamicJar.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JarFile ownJar = new JarFile(ownFile);

            Enumeration<JarEntry> entries = ownJar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(path)) {
                    continue;
                }
                if (entry.getName().endsWith(".jar")) {
                    logger.debug("Found lib " + entry.getName());
                    URL url = new URL("jar", "", ownFile.toURI().toString() + "!/" + entry.getName());
                    libs.add(url);
                } else if (entry.getName().endsWith(".ref")) {
                    InputStream inputStream = ownJar.getInputStream(entry);
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    if (scanner.hasNext()) {
                        String target = scanner.next();
                        URL url = new URL("jar", "", ownFile.toURI().toString() + "!/" + target);
                        libs.add(url);
                    }
                }
            }
            return libs.toArray(new URL[libs.size()]);
        } catch (IOException | URISyntaxException e) {
            throw new NestedJarClassloaderException(e);
        }
    }

    public static NestedJarClassloader initiate(ClassLoader classLoader, DynamicJarConfiguration configuration)

        throws NestedJarClassloaderException {
        if (!(classLoader instanceof NestedJarClassloader)) {
            classLoader = new NestedJarClassloader(new URL[] {}, classLoader);
        }

        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Class<?> dynamicJarClass = Class.forName(DynamicJar.class.getName(), true, classLoader);
            Method initiateMethod = dynamicJarClass.getDeclaredMethod("initiate", byte[].class, Object.class);
            initiateMethod.setAccessible(true);
            return (NestedJarClassloader) initiateMethod
                .invoke(null, ConfigurationSerializer.serializeConfig(configuration), classLoader);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException | IOException e) {
            throw new NestedJarClassloaderException(e);
        }
    }

    private static NestedJarClassloader initiate(byte[] configurationBytes, Object loadingJarLibClassLoader)
        throws DynamicJarException {
        logger.info("Initiating DynamicJar");
        DynamicJarConfiguration configuration = ConfigurationSerializer.deserializeConfig(configurationBytes);
        URL libs[] = getLibs("META-INF/lib/");
        NestedJarClassloader isolatedClassLoader = new NestedJarClassloader(libs, null);

        Set<String> loadedLibs = new HashSet<>();
        for (URL url : libs) {
            loadedLibs.add(FilenameUtils.getName(url.getFile()));
        }

        RemoteDependencyLoader
            .loadDependencies(isolatedClassLoader, (NestedJarClassloader) loadingJarLibClassLoader, configuration,
                loadedLibs);
        Thread.currentThread().setContextClassLoader((ClassLoader) loadingJarLibClassLoader);

        FrameworkProviderService.loadProviders(isolatedClassLoader, configuration);
        logger.info("DynamicJar initiated.");
        return isolatedClassLoader;
    }

    public static void initiate(Class forClass) throws DynamicJarException {
        initiate(forClass, getConfiguration());
    }

    public static NestedJarClassloader initiate(Class forClass, DynamicJarConfiguration configuration)
        throws DynamicJarException {
        return initiate(forClass.getClassLoader(), configuration);
    }

    public static NestedJarClassloader initiate(ClassLoader forClassLoader) throws DynamicJarException {
        return initiate(forClassLoader, getConfiguration());
    }

}
