package io.squark.ask.core.main;

import io.squark.ask.core.api.model.AskConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-06-14.
 * Copyright 2016
 */
public class AskTest {

    private static boolean invoked;

    @Test
    public void testLoadMainClass() throws Exception {
        Class<? extends Ask> askClass = Ask.class;
        Method loadMainClassMethod = askClass.getDeclaredMethod("loadMainClass", ClassLoader.class,
                AskConfiguration.class, String[].class);
        loadMainClassMethod.setAccessible(true);
        AskConfiguration askConfiguration = new AskConfiguration();
        askConfiguration.setMainClass(TestMainClass.class.getName());
        loadMainClassMethod.invoke(null, this.getClass().getClassLoader(), askConfiguration, new String[] {});
        Assert.assertTrue(invoked);
    }

    private static class TestMainClass {

        public static void main(String[] args) {
            invoked = true;
        }
    }
}