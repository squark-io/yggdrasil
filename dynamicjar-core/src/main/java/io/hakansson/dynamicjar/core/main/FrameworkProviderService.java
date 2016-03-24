package io.hakansson.dynamicjar.core.main;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
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

    public static void loadProviders(ClassLoader classLoader) {
        logger.debug(Arrays.toString(((URLClassLoader) classLoader).getURLs()));

        final ServiceLoader<FrameworkProvider> loader = ServiceLoader.load(FrameworkProvider.class, null);

        try {
            Iterator<FrameworkProvider> providerIterator = loader.iterator();
            if (!providerIterator.hasNext()) {
                logger.info("No FrameworkProviders found");
                if (logger.isDebugEnabled()) {
                    try {
                        Enumeration<URL> urls = classLoader.getResources("META-INF/services/" + FrameworkProvider.class
                                .getName());
                        while (urls.hasMoreElements()) {
                            logger.debug(Marker.ANY_MARKER, "Classloader has the following service file: " + urls.nextElement().toString());
                        }
                    } catch (IOException e) {
                        logger.error(Marker.ANY_MARKER, e);
                    }
                }
            }
            while (providerIterator.hasNext()) {
                FrameworkProvider provider = providerIterator.next();
                logger.info("Loading FrameworkProvider " + provider.getClass().getSimpleName());
                provider.provide();
            }
        } catch (ServiceConfigurationError serviceError) {
            logger.error(Marker.ANY_MARKER, serviceError);
        }
    }

}
