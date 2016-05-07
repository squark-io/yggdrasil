package io.hakansson.dynamicjar.core.main.factory;

import io.hakansson.dynamicjar.core.api.DependencyResolutionProvider;
import io.hakansson.dynamicjar.core.api.exception.DependencyResolutionException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.core.main.DynamicJar;
import io.hakansson.dynamicjar.nestedjarclassloader.NestedJarClassloader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ServiceLoader;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-21.
 * Copyright 2016
 */
public class DependencyResolutionProviderFactory {

    private static Logger logger = LoggerFactory.getLogger(DynamicJar.class);

    public static Collection<DependencyResolutionProvider> getDependencyResolvers(
        DynamicJarConfiguration configuration, NestedJarClassloader helperClassloader)
        throws DependencyResolutionException {

        if (configuration != null &&
            StringUtils.isNotEmpty(configuration.getDependencyResolutionProviderClass())) {
            try {
                //noinspection unchecked
                Class<? extends DependencyResolutionProvider> clazz =
                    (Class<? extends DependencyResolutionProvider>) Class
                        .forName(configuration.getDependencyResolutionProviderClass(), true,
                            helperClassloader);
                return Collections.singletonList(clazz.newInstance());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new DependencyResolutionException(e);
            }
        }

        ServiceLoader<DependencyResolutionProvider> loader =
            ServiceLoader.load(DependencyResolutionProvider.class, helperClassloader);
        Collection<DependencyResolutionProvider> providers = new ArrayList<>();
        for (DependencyResolutionProvider provider : loader) {
            providers.add(provider);
        }

        return providers;
    }
}
