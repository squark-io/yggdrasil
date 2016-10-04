package io.squark.yggdrasil.core.main;

import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-06-14.
 * Copyright 2016
 */
public class YggdrasilTest {

    private static boolean invoked;

    @Test
    public void testLoadMainClass() throws Exception {
        Class<? extends Yggdrasil> yggdrasilClass = Yggdrasil.class;
        Method loadMainClassMethod = yggdrasilClass.getDeclaredMethod("loadMainClass", ClassLoader.class,
                YggdrasilConfiguration.class, String[].class);
        loadMainClassMethod.setAccessible(true);
        YggdrasilConfiguration yggdrasilConfiguration = new YggdrasilConfiguration();
        yggdrasilConfiguration.setMainClass(TestMainClass.class.getName());
        loadMainClassMethod.invoke(null, this.getClass().getClassLoader(), yggdrasilConfiguration, new String[] {});
        Assert.assertTrue(invoked);
    }

    private static class TestMainClass {

        public static void main(String[] args) {
            invoked = true;
        }
    }
}