package io.hakansson.dynamicjar.module.logging;

import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.LoggingModule;
import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public class Log4j2LoggingModule implements LoggingModule {

    private static final List<String> validConfigFiles = new ArrayList<>();

    static {
        validConfigFiles.add("log4j2.properties");
        validConfigFiles.add("log4j2.yaml");
        validConfigFiles.add("log4j2.yml");
        validConfigFiles.add("log4j2.json");
        validConfigFiles.add("log4j2.jsn");
        validConfigFiles.add("log4j2.xml");
    }

    @Override
    public void initialize(@Nullable DynamicJarConfiguration configuration, @Nullable ClassLoader classLoader,
        @Nullable URL jarWithConfig) throws DynamicJarException {

        String systemLogLevel = System.getProperty(Constants.DYNAMICJAR_LOG_LEVEL);
        String systemConfigFile = System.getProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY);
        URI configuredConfigFile = null;
        String configuredLogLevel = null;
        if (configuration != null && configuration.getLoggerConfiguration() != null) {
            configuredConfigFile = configuration.getLoggerConfiguration().getConfigFile() != null ?
                                   URI.create(configuration.getLoggerConfiguration().getConfigFile()) : null;
            configuredLogLevel = configuration.getLoggerConfiguration().getDefaultLogLevel();
        }

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
        if (systemLogLevel != null) {
            final Configuration config = loggerContext.getConfiguration();
            config.getRootLogger().setLevel(Level.getLevel(systemLogLevel));
        } else if (systemConfigFile != null) {
            configuredConfigFile = URI.create(systemConfigFile);
            loggerContext.setConfigLocation(configuredConfigFile);
        } else if (configuredLogLevel != null) {
            final Configuration config = loggerContext.getConfiguration();
            config.getRootLogger().setLevel(Level.getLevel(configuredLogLevel));
        } else if (configuredConfigFile != null) {
            loggerContext.setConfigLocation(configuredConfigFile);
        } else if (jarWithConfig != null) {
            try {
                InputStream inputStream = jarWithConfig.openStream();
                JarInputStream jarInputStream = new JarInputStream(inputStream);
                JarEntry jarEntry;

                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) continue;
                    String fileName = FilenameUtils.getName(jarEntry.getName());
                    if (validConfigFiles.contains(fileName)) {
                        configuredConfigFile = URI.create(jarWithConfig.getFile() + "!/" + jarEntry.getName());
                        loggerContext.setConfigLocation(configuredConfigFile);
                        break;
                    }
                }
            } catch (IOException e) {
                throw new DependencyResolutionException(e);
            }
        }
    }
}
