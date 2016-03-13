package org.dynamicjar.core.main;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.dynamicjar.core.api.exception.DynamicJarException;
import org.dynamicjar.core.api.exception.PropertyLoadException;
import org.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.io.InputStreamReader;

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

    public static void main(String[] args) {
        try {
            initiate(DynamicJar.class);
        } catch (DynamicJarException e) {
            logger.error(Marker.ANY_MARKER, e);
        }
    }

    private DynamicJar() {
        //Disallow instantiation
    }

    public static void initiate(ClassLoader forClassLoader)
        throws DynamicJarException {
        DependencyResolutionHandler.loadDependencies(forClassLoader, getConfiguration());
        FrameworkProviderService.loadProviders(forClassLoader);
    }

    public static void initiate(Class forClass)
        throws DynamicJarException {
        DependencyResolutionHandler.loadDependencies(forClass.getClassLoader(), getConfiguration());
        FrameworkProviderService.loadProviders(forClass.getClassLoader());
    }

    private static DynamicJarConfiguration getConfiguration() throws PropertyLoadException {
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
        return dynamicJarConfiguration;
    }

}
