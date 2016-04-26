package io.hakansson.dynamicjar.core.api;

import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.api.util.ConfigurationSerializer;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public class FrameworkProviderService {

    private static final Logger logger = LoggerFactory.getLogger(FrameworkProviderService.class);

    private static void loadProviders(byte[] configurationAsBytes) throws DynamicJarException {

        Thread.currentThread()
            .setContextClassLoader(FrameworkProviderService.class.getClassLoader());
        DynamicJarConfiguration configuration =
            ConfigurationSerializer.deserializeConfig(configurationAsBytes);

        final ServiceLoader<FrameworkProvider> loader = ServiceLoader
            .load(FrameworkProvider.class, FrameworkProviderService.class.getClassLoader());

        try {
            Iterator<FrameworkProvider> providerIterator = loader.iterator();
            if (!providerIterator.hasNext()) {
                logger.info("No FrameworkProviders found");
            } else {
                while (providerIterator.hasNext()) {
                    FrameworkProvider provider = providerIterator.next();
                    logger.info("Loading FrameworkProvider " + provider.getClass().getSimpleName());
                    provider.provide(configuration);
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            logger.error(Marker.ANY_MARKER, serviceError);
        }
    }

    public static void loadProviders(NestedJarClassloader classLoader,
        DynamicJarConfiguration configuration) {

        try {
            byte[] serializedConfig = ConfigurationSerializer.serializeConfig(configuration);

            Class<?> selfClass = classLoader.loadClass(FrameworkProviderService.class.getName());
            Method loadProvidersMethod = selfClass.getDeclaredMethod("loadProviders", byte[].class);
            loadProvidersMethod.setAccessible(true);
            loadProvidersMethod.invoke(null, (Object) serializedConfig);
        } catch (Exception e) {
            logger.error(Marker.ANY_MARKER, e);
        }
    }

}
