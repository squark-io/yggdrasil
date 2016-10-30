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
package io.squark.yggdrasil.core.main.factory;

import io.squark.nestedjarclassloader.NestedJarClassLoader;
import io.squark.yggdrasil.core.api.DependencyResolutionProvider;
import io.squark.yggdrasil.logging.api.CrappyLogger;
import io.squark.yggdrasil.logging.api.LogLevel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-10-30.
 * Copyright 2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceLoader.class, DependencyResolutionProviderFactory.class})
public class DependencyResolutionProviderFactoryTest {

    @Before
    public void setup() {
        System.setProperty(CrappyLogger.YGGDRASIL_LOG_LEVEL, LogLevel.DEBUG.name());
    }

    @After
    public void tearDown() {
        System.clearProperty(CrappyLogger.YGGDRASIL_LOG_LEVEL);
    }

    @Test
    public void getDependencyResolvers() throws Exception {
        DependencyResolutionProvider providerMock = PowerMockito.mock(DependencyResolutionProvider.class);
        PowerMockito.spy(DependencyResolutionProviderFactory.class);
        ServiceLoader mockServiceLoader = PowerMockito.mock(ServiceLoader.class);
        Mockito.when(mockServiceLoader.iterator()).thenReturn(new Iterator() {

            boolean returned;

            @Override
            public boolean hasNext() {
                return !returned;
            }

            @Override
            public Object next() {
                returned = true;
                return providerMock;
            }
        });
        PowerMockito.mockStatic(ServiceLoader.class, invocationOnMock -> {
            if (Objects.equals(invocationOnMock.getMethod().getName(), "load")) {
                return mockServiceLoader;
            } else return invocationOnMock.callRealMethod();
        });


        Collection<DependencyResolutionProvider> list = DependencyResolutionProviderFactory
            .getDependencyResolvers(new NestedJarClassLoader(this.getClass().getClassLoader()));
        Assert.assertEquals(1, list.size());
    }

}