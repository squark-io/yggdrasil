package io.hakansson.dynamicjar.core.it;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * *** DynamicJar ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-13.
 * Copyright 2016
 */
public class JsonConfigFileTestTarget {
    private Logger logger;

    public void helloWorld() {
        logger = LogManager.getLogger(JsonConfigFileTestTarget.class);
        logger.info(this.getClass().getSimpleName() + ": Hello world");
    }
}
