package io.hakansson.dynamicjar.frameworkprovider.db;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-28.
 * Copyright 2016
 */
public class DatabaseFrameworkProvider implements FrameworkProvider {

    private final Logger logger = LoggerFactory.getLogger(DatabaseFrameworkProvider.class);

    @Override
    public void provide(@Nullable DynamicJarConfiguration configuration) throws DynamicJarException {

        //TODO: Rename provider to JPA provider.
        //TODO: Add logging everywhere.

        logger.info("Initializing " + DatabaseFrameworkProvider.class.getSimpleName() + "...");

        //The only thing we need to to is add some properties.
        System.setProperty(org.hibernate.cfg.AvailableSettings.SCANNER_ARCHIVE_INTERPRETER,
                CustomArchiveDescriptorFactory.class.getName());
        System.setProperty(org.hibernate.jpa.AvailableSettings.TRANSACTION_TYPE, "RESOURCE_LOCAL");

        logger.info(DatabaseFrameworkProvider.class.getSimpleName() + " initialized.");
    }

    @Override
    public String getName() {
        return DatabaseFrameworkProvider.class.getSimpleName();
    }

    @Override
    public List<ProviderDependency> runBefore() {
        return Collections.singletonList(new ProviderDependency("WeldFrameworkProvider", true));
    }
}
