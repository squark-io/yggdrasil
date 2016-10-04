package io.squark.yggdrasil.core.api;

import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.core.api.util.ConfigurationSerializer;
import io.squark.yggdrasil.core.api.util.FrameworkProviderComparator;
import io.squark.yggdrasil.core.api.util.FrameworkProviderUtil;
import io.squark.yggdrasil.core.api.util.ReflectionUtil;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import io.squark.yggdrasil.nestedjarclassloader.NestedJarClassLoader;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-03-11.
 * Copyright 2016
 */
public class FrameworkProviderService {

    private static final Logger logger = InternalLoggerBinder.getLogger(FrameworkProviderService.class);

    @SuppressWarnings("unused")
    private static void loadProviders(byte[] configurationAsBytes) throws YggdrasilException {

        Thread.currentThread().setContextClassLoader(FrameworkProviderService.class.getClassLoader());
        YggdrasilConfiguration configuration = ConfigurationSerializer.deserializeConfig(configurationAsBytes);

        final ServiceLoader<FrameworkProvider> loader = ServiceLoader.load(FrameworkProvider.class,
                FrameworkProviderService.class.getClassLoader());

        try {
            Iterator<FrameworkProvider> providerIterator = loader.iterator();
            if (!providerIterator.hasNext()) {
                logger.info("No FrameworkProviders found");
            } else {
                List<FrameworkProvider> providerList = IteratorUtils.toList(providerIterator);
                FrameworkProviderUtil.validateDependencies(providerList);
                Collections.sort(providerList, new FrameworkProviderComparator());

                for (FrameworkProvider provider : providerList) {
                    logger.info("Loading FrameworkProvider " + provider.getClass().getSimpleName());
                    provider.provide(configuration);
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            logger.error(Marker.ANY_MARKER, serviceError);
        }
    }

    public static void loadProviders(NestedJarClassLoader classLoader, YggdrasilConfiguration configuration) {

        try {
            byte[] serializedConfig = ConfigurationSerializer.serializeConfig(configuration);

            ReflectionUtil.invokeMethod("loadProviders", FrameworkProviderService.class.getName(), null,
                    new Object[]{serializedConfig}, null, classLoader, null);
        } catch (Throwable e) {
            logger.error(Marker.ANY_MARKER, e);
        }
    }

}
