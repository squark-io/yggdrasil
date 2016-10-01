package io.squark.ask.frameworkprovider;

import io.squark.ask.core.api.AskContext;
import io.squark.ask.core.api.FrameworkProvider;
import io.squark.ask.core.api.exception.AskException;
import io.squark.ask.core.api.model.AskConfiguration;
import io.squark.ask.logging.api.InternalLoggerBinder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.BeanManager;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
public class WeldFrameworkProvider implements FrameworkProvider {

    private static final Logger logger = InternalLoggerBinder.getLogger(WeldFrameworkProvider.class);

    @Override
    public void provide(AskConfiguration configuration) throws AskException {
        logger.info("Initializing Weld container...");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        weld.setClassLoader(WeldFrameworkProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        AskContext.registerObject(BeanManager.class.getName(), container.getBeanManager());
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
