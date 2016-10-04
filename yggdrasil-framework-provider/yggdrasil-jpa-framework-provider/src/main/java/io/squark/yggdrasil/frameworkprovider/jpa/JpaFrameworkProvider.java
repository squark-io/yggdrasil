package io.squark.yggdrasil.frameworkprovider.jpa;

import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-05-28.
 * Copyright 2016
 */
public class JpaFrameworkProvider implements FrameworkProvider {

    private final Logger logger = InternalLoggerBinder.getLogger(JpaFrameworkProvider.class);

    @Override
    public void provide(@Nullable YggdrasilConfiguration configuration) throws YggdrasilException {
        logger.info("Initializing " + JpaFrameworkProvider.class.getSimpleName() + "...");

        //The only thing we need to to is add some properties.
        System.setProperty(org.hibernate.cfg.AvailableSettings.SCANNER_ARCHIVE_INTERPRETER,
                CustomArchiveDescriptorFactory.class.getName());
        System.setProperty(org.hibernate.jpa.AvailableSettings.TRANSACTION_TYPE, "RESOURCE_LOCAL");

        logger.info(JpaFrameworkProvider.class.getSimpleName() + " initialized.");
    }

    @Override
    public String getName() {
        return JpaFrameworkProvider.class.getSimpleName();
    }

    @Override
    public List<ProviderDependency> runBefore() {
        return Collections.singletonList(new ProviderDependency("WeldFrameworkProvider", true));
    }
}
