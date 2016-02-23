package org.dynamicjar.test;

import org.dynamicjar.core.api.exception.DependencyResolutionException;
import org.dynamicjar.core.main.DynamicJar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-02-15.
 * Copyright 2016
 */
public class DependencyResolutionIntegrationTest {

    private static final String LOG_4_J_CLASS_NAME = "org.apache.logging.log4j.core.Logger";

    /**
     * Need separate Classes for each test to force classloader to reload class.
     */
    @Test(expectedExceptions = NoClassDefFoundError.class, priority = 1)
    public void noClassLoadedIntegrationTest() {
        new TestTarget1().helloWorld();
    }

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
        InstantiationException {
        DynamicJar.loadDependencies("org.dynamicjar.dynamicjar-test", "basic-test", TestTarget2.class);
        TestTarget2 business = new TestTarget2();
        business.helloWorld();
        assertEquals(business.getLogger().getClass().getName(), LOG_4_J_CLASS_NAME);
    }
}