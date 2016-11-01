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
import io.squark.yggdrasil.core.api.util.LibHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockGateway;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.powermock.api.mockito.PowerMockito.doReturn;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Yggdrasil.class, LibHelper.class, JarFile.class})
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

    @Test
    public void getClassesJarTest() throws Exception {
        YggdrasilConfiguration configuration = new YggdrasilConfiguration();
        configuration.setClassesJar("dummy.jar");
        URL jar = Yggdrasil.getClassesJar(configuration);
        Assert.assertTrue(jar != null);
        Assert.assertTrue(jar.getFile().endsWith("dummy.jar"));
        URL ownJar = new File("").toURI().toURL();
        PowerMockito.mockStatic(LibHelper.class, invocationOnMock -> {
            if (invocationOnMock.getMethod().getName().equals("getOwnJar")) {
                return ownJar;
            }
            return invocationOnMock.callRealMethod();
        });
        JarFile jarFileMock = Mockito.mock(JarFile.class);
        PowerMockito.mockStatic(JarFile.class);
        PowerMockito.whenNew(JarFile.class).withArguments(new File(ownJar.toURI())).then(invocationOnMock -> jarFileMock);
        Mockito.when(jarFileMock.entries()).thenReturn(new Enumeration<JarEntry>() {

            boolean returned = false;

            @Override
            public boolean hasMoreElements() {
                return !returned;
            }

            @Override
            public JarEntry nextElement() {
                return new JarEntry("dummy.jar");
            }
        });
        jar = Yggdrasil.getClassesJar(configuration);
        Assert.assertNotNull(jar);
        Assert.assertEquals("file:" + new File("").toURI().toURL().getFile() + "!/dummy.jar", jar.getFile());
    }

    private static class TestMainClass {

        private static boolean invoked;

        public static void main(String[] args) {
            invoked = true;
        }
    }
}