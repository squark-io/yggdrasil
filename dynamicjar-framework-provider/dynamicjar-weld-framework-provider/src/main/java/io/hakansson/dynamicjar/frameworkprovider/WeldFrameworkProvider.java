package io.hakansson.dynamicjar.frameworkprovider;

import io.hakansson.dynamicjar.core.api.DynamicJarContext;
import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.frameworkprovider.exception.CDIException;
import io.hakansson.dynamicjar.logging.api.InternalLoggerBinder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.BeanManager;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
public class WeldFrameworkProvider implements FrameworkProvider {

    private static final Logger logger = InternalLoggerBinder.getLogger(WeldFrameworkProvider.class);

    @Override
    public void provide(DynamicJarConfiguration configuration) throws CDIException {
        logger.info("Initializing Weld container...");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        weld.setClassLoader(WeldFrameworkProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        DynamicJarContext.registerObject(BeanManager.class.getName(), container.getBeanManager());
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
