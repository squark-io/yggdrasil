package io.hakansson.dynamicjar.core.api.logging;

import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.exception.NestedJarClassloaderException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.core.api.util.LibHelper;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

        if (loggingModule != null) {
            if (internalLogging) logger.info("Initiated logging using " + loggingModule.getClass().getSimpleName());
        } else {
            if (classLoader.getClass().getName().equals(NestedJarClassLoader.class.getName())) {
                if (internalLogging) logger.info("No logging module found. Trying to load fallback console logger...");
                URL[] loggerFallbackURLs = LibHelper.getLibs(
                        Constants.DYNAMICJAR_RUNTIME_OPTIONAL_LIB_PATH + Constants.DYNAMIC_JAR_LOGGING_API_ARTIFACT_ID +
                                "-fallback.jar");
                if (loggerFallbackURLs.length >= 1) {
                    if (internalLogging) logger.info("Found fallback logger. Loading...");
                    try {
                        Method addURLs = classLoader.getClass().getDeclaredMethod("addURLs", URL[].class);
                        addURLs.invoke(classLoader, (Object) loggerFallbackURLs);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new NestedJarClassloaderException(e);
                    }
                }
            } else if (internalLogging) {
                logger.info("No logging module found. May not get logging in thirdparty libraries");
            }
        }
    }

}