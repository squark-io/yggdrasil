package org.dynamicjar.test;

import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.api.exception.PropertyLoadException;
import org.dynamicjar.core.main.DynamicJar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-15.
 * Copyright 2016
 */
public class YamlConfigFileIntegrationTest {

    private static final String LOG_4_J_CLASS_NAME = "org.apache.logging.log4j.core.Logger";

    /**
     * Need separate Classes for each test to force classloader to reload class.
     *
     * @throws DependencyResolutionException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(priority = 2)
    public void loadJarIntegrationTest()
        throws DependencyResolutionException, ClassNotFoundException, IllegalAccessException,
        InstantiationException, PropertyLoadException {
        DynamicJar.loadDependencies(YamlConfigFileTestTarget.class);
        YamlConfigFileTestTarget business = new YamlConfigFileTestTarget();
        business.helloWorld();
        assertEquals(business.getLogger().getClass().getName(), LOG_4_J_CLASS_NAME);
    }
}