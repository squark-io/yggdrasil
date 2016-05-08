package io.hakansson.dynamicjar.module.logging;

import io.hakansson.dynamicjar.core.api.LoggingModule;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-08.
 * Copyright 2016
 */
public class Log4j2LoggingModule implements LoggingModule {

    private static final String DEFAULT_LOG_LEVEL = "INFO";

    @Override
    public void initialize(@Nullable URI configuration, @Nullable String rootLogLevel)
        throws DynamicJarException {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
        if (configuration != null) {
            loggerContext.setConfigLocation(configuration);
        } else {
            Level logLevel;
            if (rootLogLevel != null) {
                logLevel = Level.getLevel(rootLogLevel);
            } else {
                logLevel =
                    Level.getLevel(System.getProperty("dynamicjar.logLevel", DEFAULT_LOG_LEVEL));
            }
            final Configuration config = loggerContext.getConfiguration();
            config.getRootLogger().setLevel(logLevel);
        }
    }
}
