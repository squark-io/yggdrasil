package io.hakansson.dynamicjar.frameworkprovider;

import io.hakansson.dynamicjar.core.api.DynamicJarContext;
import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
public class WeldFrameworkProvider implements FrameworkProvider {

    private static final Logger logger = LoggerFactory.getLogger(WeldFrameworkProvider.class);

    @Override
    public void provide(DynamicJarConfiguration configuration) {
        //org.jboss.weld.se.scan.classpath.entries=true med Weld.property() för att slippa beans.xml. Gör configurerbart med true som default
        logger.info("Initializing Weld container...");
        Weld weld = new Weld();
        weld.setClassLoader(WeldFrameworkProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        DynamicJarContext.registerObject(BeanManager.class.getName(), container.getBeanManager());
        logger.info(WeldFrameworkProvider.class.getSimpleName() + " initialized.");
    }
}
