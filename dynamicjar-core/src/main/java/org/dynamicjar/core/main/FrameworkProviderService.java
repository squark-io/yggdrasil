package org.dynamicjar.core.main;

import org.dynamicjar.core.api.FrameworkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
        final ServiceLoader<FrameworkProvider> loader = ServiceLoader.load(FrameworkProvider.class, classLoader);

        try {
            Iterator<FrameworkProvider> providerIterator = loader.iterator();
            if (!providerIterator.hasNext()) {
                logger.info("No FrameworkProviders found");
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
