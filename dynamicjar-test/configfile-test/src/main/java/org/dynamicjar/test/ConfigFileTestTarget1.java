package org.dynamicjar.test;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-13.
 * Copyright 2016
 */
public class ConfigFileTestTarget1 {
    private Logger logger;

    public void helloWorld() {
        logger = LogManager.getLogger(ConfigFileTestTarget1.class);
        logger.info("Hello world");
    }

    public Object getLogger() {
        return logger;
    }
}
