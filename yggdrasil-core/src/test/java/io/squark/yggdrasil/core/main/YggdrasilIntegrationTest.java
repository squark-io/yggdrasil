package io.squark.yggdrasil.core.main;

import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.YggdrasilContext;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockGateway;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.lang.reflect.Method;

import static org.powermock.api.mockito.PowerMockito.doReturn;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-06-14.
 * Copyright 2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Yggdrasil.class})
public class YggdrasilIntegrationTest {

    private InputStream yamlInputStream = getClass().getResourceAsStream(Constants.YAML_PROPERTIES_FILE);
    private InputStream jsonInputStream = getClass().getResourceAsStream(Constants.JSON_PROPERTIES_FILE);

    @Test
    public void internalMainYamlTest() throws Exception {
        MockGateway.MOCK_GET_CLASS_METHOD = true;
        PowerMockito.spy(Yggdrasil.class);
        doReturn(yamlInputStream).when(Yggdrasil.class, "getFile", Constants.YAML_PROPERTIES_FILE);
        Yggdrasil.internalMain(null);
        Assert.assertEquals("YAML-TEST", YggdrasilContext.getConfiguration().getYggdrasilVersion());
    }

    @Test
    public void internalMainJsonTest() throws Exception {
        MockGateway.MOCK_GET_CLASS_METHOD = true;
        PowerMockito.spy(Yggdrasil.class);
        doReturn(null).when(Yggdrasil.class, "getFile", Constants.YAML_PROPERTIES_FILE);
        doReturn(jsonInputStream).when(Yggdrasil.class, "getFile", Constants.JSON_PROPERTIES_FILE);
        Yggdrasil.internalMain(null);
        Assert.assertEquals("JSON-TEST", YggdrasilContext.getConfiguration().getYggdrasilVersion());
    }

    @Test
    public void loadMainClassTest() throws Exception {
        Class<? extends Yggdrasil> yggdrasilClass = Yggdrasil.class;
        Method loadMainClassMethod = yggdrasilClass.getDeclaredMethod("loadMainClass", ClassLoader.class,
                YggdrasilConfiguration.class, String[].class);
        loadMainClassMethod.setAccessible(true);
        YggdrasilConfiguration yggdrasilConfiguration = new YggdrasilConfiguration();
        yggdrasilConfiguration.setMainClass(TestMainClass.class.getName());
        loadMainClassMethod.invoke(null, this.getClass().getClassLoader(), yggdrasilConfiguration, new String[] {});
        Assert.assertTrue(TestMainClass.invoked);
    }

    private static class TestMainClass {

        private static boolean invoked;

        public static void main(String[] args) {
            invoked = true;
        }
    }
}