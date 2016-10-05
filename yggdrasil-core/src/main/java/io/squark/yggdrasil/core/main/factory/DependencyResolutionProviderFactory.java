package io.squark.yggdrasil.core.main.factory;

import io.squark.yggdrasil.core.api.DependencyResolutionProvider;
import io.squark.yggdrasil.core.api.exception.DependencyResolutionException;
import io.squark.yggdrasil.core.main.Yggdrasil;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.squark.nestedjarclassloader.NestedJarClassLoader;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-02-21.
 * Copyright 2016
 */
public class DependencyResolutionProviderFactory {

    private static Logger logger = InternalLoggerBinder.getLogger(Yggdrasil.class);

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
