package io.hakansson.dynamicjar.weld;

import io.hakansson.dynamicjar.core.api.FrameworkProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-27.
 * Copyright 2016
 */
public class WeldFrameworkProvider implements FrameworkProvider {

    private static final Logger logger = LoggerFactory.getLogger(WeldFrameworkProvider.class);

    @Override
    public void provide() {
        logger.info("Initializing Weld container...");
        Weld weld = new Weld();
        weld.addExtension(new ExcludePackageCDIExtension());
        WeldContainer container = weld.initialize();
        logger.info("Weld container initialized.");
    }
}
