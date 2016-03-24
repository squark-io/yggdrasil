package io.hakansson.dynamicjar.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.MainClassLoadException;
import io.hakansson.dynamicjar.core.api.exception.PropertyLoadException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public static void main(String[] args) {
        try {
            DynamicJarConfiguration configuration = getConfiguration();
            initiate(DynamicJar.class, configuration);
            loadMainClass(DynamicJar.class.getClassLoader(), configuration, args);
        } catch (DynamicJarException e) {
            logger.error(Marker.ANY_MARKER, e);
        }
    }

    public static void initiate(Class forClass) throws DynamicJarException {
        initiate(forClass, getConfiguration());
    }

    public static void initiate(ClassLoader forClassLoader) throws DynamicJarException {
        initiate(forClassLoader, getConfiguration());
    }

    public static void initiate(Class forClass, DynamicJarConfiguration configuration) throws DynamicJarException {
        initiate(forClass.getClassLoader(), configuration);
    }

    public static void initiate(ClassLoader forClassLoader, DynamicJarConfiguration configuration)
        throws DynamicJarException {
        DependencyResolutionHandler.loadDependencies(forClassLoader, configuration);
        FrameworkProviderService.loadProviders(forClassLoader);
    }

    private static DynamicJarConfiguration configuration;

    private static DynamicJarConfiguration getConfiguration() throws PropertyLoadException {
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
                        dynamicJarConfiguration = gson.fromJson(new InputStreamReader(inputStream),
                            DynamicJarConfiguration.class);
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

    private static void loadMainClass(ClassLoader forClassLoader,
        DynamicJarConfiguration configuration, String[] args) throws MainClassLoadException {
        //Load main class:
        if (StringUtils.isNotEmpty(configuration.getMainClass())) {
            try {
                Class<?> mainClass =
                    Class.forName(configuration.getMainClass(), true, forClassLoader);
                Object mainClassInstance = mainClass.newInstance();
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(mainClassInstance, args);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
                throw new MainClassLoadException(e);
            }
        }
    }

}
