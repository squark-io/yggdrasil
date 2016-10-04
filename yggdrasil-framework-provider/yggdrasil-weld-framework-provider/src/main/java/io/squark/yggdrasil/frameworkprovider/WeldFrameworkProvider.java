package io.squark.yggdrasil.frameworkprovider;

import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.FrameworkProvider;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.BeanManager;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
public class WeldFrameworkProvider implements FrameworkProvider {

    private static final Logger logger = InternalLoggerBinder.getLogger(WeldFrameworkProvider.class);

    @Override
    public void provide(YggdrasilConfiguration configuration) throws YggdrasilException {
        logger.info("Initializing Weld container...");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        weld.setClassLoader(WeldFrameworkProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        YggdrasilContext.registerObject(BeanManager.class.getName(), container.getBeanManager());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                container.shutdown();
            }
        });
        logger.info(WeldFrameworkProvider.class.getSimpleName() + " initialized.");
    }

    @Override
    public String getName() {
        return WeldFrameworkProvider.class.getSimpleName();
    }
}
