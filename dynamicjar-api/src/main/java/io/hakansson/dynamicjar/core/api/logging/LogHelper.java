package io.hakansson.dynamicjar.core.api.logging;

import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.core.api.util.LibHelper;
import io.hakansson.dynamicjar.core.api.util.ReflectionUtil;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ServiceLoader;

/**
 * Created by Erik Håkansson on 2016-05-09.
 * WirelessCar
 */
public class LogHelper {

    private static Logger logger = InternalLoggerBinder.getLogger(LogHelper.class);
    private static String FALLBACK_LOGGING_MODULE_NAME = "FALLBACK_LOGGER";
    private static boolean fallbackLoaded = false;

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
                loggingModule = module;
            } else {
                throw new IllegalStateException("Found multiple LoggingModules. Only one can be deployed!");
            }
        }

        if (loggingModule != null) {
            if (fallbackLoaded && classLoader.getClass().getName().equals(NestedJarClassLoader.class.getName())) {
                logger.info("Unloading fallback loader and replacing it with " + loggingModule.getClass().getSimpleName());
                try {
                    ReflectionUtil.invokeMethod("unloadModule", NestedJarClassLoader.class.getName(), classLoader,
                            new Object[]{FALLBACK_LOGGING_MODULE_NAME}, null, null, null);
                } catch (Throwable e) {
                    throw new DynamicJarException(e);
                }
            } else {
                logger.debug("No fallback logger loaded. Not unloading anything.");
            }
            ILoggerFactory iLoggerFactory = loggingModule.initialize(configuration, classLoader, jarWithConfig);
            InternalLoggerBinder.getSingleton().notifyLoggingInitialized(iLoggerFactory);
            if (internalLogging) logger.info("Initiated logging using " + loggingModule.getClass().getSimpleName());
        } else {
            if (classLoader.getClass().getName().equals(NestedJarClassLoader.class.getName())) {
                if (internalLogging) logger.info("No logging module found. Trying to load fallback console logger...");
                URL[] loggerFallbackURLs = LibHelper.getLibs(LogHelper.class,
                        Constants.DYNAMICJAR_RUNTIME_OPTIONAL_LIB_PATH + Constants.DYNAMIC_JAR_LOGGING_API_ARTIFACT_ID +
                                "-fallback.jar");
                if (loggerFallbackURLs.length >= 1) {
                    if (internalLogging) logger.info("Found fallback logger. Loading...");
                    try {
                        ReflectionUtil.invokeMethod("addURLs", classLoader.getClass().getName(), classLoader,
                                new Object[]{FALLBACK_LOGGING_MODULE_NAME, loggerFallbackURLs}, null, null, null);
                    } catch (Throwable e) {
                        throw new DynamicJarException(e);
                    }
                    fallbackLoaded = true;
                } else {
                    logger.warn("Failed to find fallback logger. May not get logging in thirdparty libraries");
                }
            } else if (internalLogging) {
                logger.info("No logging module found. May not get logging in thirdparty libraries");
            }
        }
    }

}
