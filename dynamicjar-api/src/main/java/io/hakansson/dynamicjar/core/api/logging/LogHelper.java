package io.hakansson.dynamicjar.core.api.logging;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ServiceLoader;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class LogHelper {

    private static Logger logger = InternalLoggerBinder.getLogger(LogHelper.class);

    @SuppressWarnings("unused")
    public static void initiateLogging(byte[] configurationBytes, Object classLoader, @Nullable URL jarWithConfig) throws
            DynamicJarException
    {

        DynamicJarConfiguration configuration = ConfigurationSerializer.deserializeConfig(configurationBytes);
        initiateLogging(configuration, (ClassLoader) classLoader, jarWithConfig, true);
    }

    public static void initiateLogging(@Nullable DynamicJarConfiguration configuration, ClassLoader classLoader,
                                       @Nullable URL jarWithConfig, boolean internalLogging) throws DynamicJarException
    {

        if (internalLogging) {
            logger.info("Initiating logging...");
        }

        ServiceLoader<LoggingModule> loggingModules = ServiceLoader.load(LoggingModule.class, classLoader);

        LoggingModule loggingModule = null;
        for (LoggingModule module : loggingModules) {
            if (loggingModule == null) {
                module.initialize(configuration, classLoader, jarWithConfig);
                loggingModule = module;
            } else {
                throw new IllegalStateException("Found multiple LoggingModules. Only one can be deployed!");
            }
        }

        if (internalLogging) {
            if (loggingModule != null) {
                logger.info("Initiated logging using " + loggingModule.getClass().getSimpleName());
            } else {
                logger.info("No logging module found. Using console logging.");
            }
        }
    }
}
