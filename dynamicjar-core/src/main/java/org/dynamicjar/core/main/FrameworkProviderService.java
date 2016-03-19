package org.dynamicjar.core.main;

import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.finder.UrlSet;
import org.dynamicjar.core.api.FrameworkProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.io.IOException;
import java.util.List;
import java.util.ServiceConfigurationError;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public class FrameworkProviderService {

    private static final Logger logger = LoggerFactory.getLogger(FrameworkProviderService.class);

    public static void loadProviders(ClassLoader classLoader) {
        ResourceFinder finder = new ResourceFinder("META-INF/services/", classLoader);
        try {
            UrlSet urlSet = new UrlSet(classLoader);
            //urlSet = urlSet.exclude(classLoader.getParent());
            //urlSet = urlSet.matching(".*dynamicjar-weld.*.jar");
            logger.debug(urlSet.getUrls().toString());
            List<Class<? extends FrameworkProvider>> implementations =
                finder.findAllImplementations(FrameworkProvider.class);
            if (implementations.size() == 0) {
                logger.info("No FrameworkProviders found");
                FrameworkProvider frameworkProvider =
                    (FrameworkProvider) Class.forName("org.dynamicjar.weld.WeldFrameworkProvider").newInstance();
                frameworkProvider.provide();
            } else {
                for (Class<? extends FrameworkProvider> implementation : implementations) {
                    logger.info("Loading FrameworkProvider " + implementation.getSimpleName());
                    FrameworkProvider provider = implementation.newInstance();
                    provider.provide();
                }

            }
        } catch (ServiceConfigurationError | ClassNotFoundException | IOException |
            IllegalAccessException | InstantiationException e) {
            logger.error(Marker.ANY_MARKER, e);
        }
    }

}
