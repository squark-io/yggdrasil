package io.hakansson.dynamicjar.core.main;

import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-14.
 * Copyright 2016
 */
public class DynamicJarTest {

    private static boolean invoked;

    @Test
    public void testLoadMainClass() throws Exception {
        Class<? extends DynamicJar> dynamicJarClass = DynamicJar.class;
        Method loadMainClassMethod = dynamicJarClass.getDeclaredMethod("loadMainClass", ClassLoader.class,
                DynamicJarConfiguration.class, String[].class);
        loadMainClassMethod.setAccessible(true);
        DynamicJarConfiguration dynamicJarConfiguration = new DynamicJarConfiguration();
        dynamicJarConfiguration.setMainClass(TestMainClass.class.getName());
        loadMainClassMethod.invoke(null, this.getClass().getClassLoader(), dynamicJarConfiguration, new String[] {});
        Assert.assertTrue(invoked);
    }

    private static class TestMainClass {

        public static void main(String[] args) {
            invoked = true;
        }
    }
}