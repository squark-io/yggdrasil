package io.hakansson.dynamicjar.core.main.factory;

import io.hakansson.dynamicjar.core.api.DependencyResolutionProvider;
import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.main.DynamicJar;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassLoader;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-21.
 * Copyright 2016
 */
public class DependencyResolutionProviderFactory {

    private static Logger logger = InternalLoggerBinder.getLogger(DynamicJar.class);

    public static Collection<DependencyResolutionProvider> getDependencyResolvers(NestedJarClassLoader helperClassloader) throws
            DependencyResolutionException
    {

        ServiceLoader<DependencyResolutionProvider> loader = ServiceLoader.load(DependencyResolutionProvider.class,
                helperClassloader);
        Collection<DependencyResolutionProvider> providers = new ArrayList<>();
        List<String> providerNames = null;
        boolean isDebug = logger.isDebugEnabled();
        if (isDebug) {
            providerNames = new ArrayList<>();
        }
        for (DependencyResolutionProvider provider : loader) {
            providers.add(provider);
            if (isDebug) {
                providerNames.add(provider.getClass().getName());
            }
        }
        logger.debug("Found providers " + providerNames);

        return providers;
    }
}
