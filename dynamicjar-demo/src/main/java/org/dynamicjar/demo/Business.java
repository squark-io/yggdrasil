package org.dynamicjar.demo;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-13.
 * Copyright 2016
 */
public class Business {

    public void helloWorld() {
        Logger logger = LogManager.getLogger(Business.class);
        logger.info("Hello world");
    }
}
