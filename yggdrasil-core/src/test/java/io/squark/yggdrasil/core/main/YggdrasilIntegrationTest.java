/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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