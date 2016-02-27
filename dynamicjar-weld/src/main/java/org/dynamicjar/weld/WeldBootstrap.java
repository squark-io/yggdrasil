package org.dynamicjar.weld;

import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.main.DynamicJar;
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
public class WeldBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(WeldBootstrap.class);

    public static void main(String[] args) {
        try {
            DynamicJar.loadDependencies("org.dynamicjar", "dynamicjar-weld",
                WeldBootstrap.class);
        } catch (DependencyResolutionException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        logger.info("Initializing Weld...");
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        logger.info("Weld initialized.");
    }
}
