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
package io.squark.yggdrasil.core.api;

import io.squark.yggdrasil.logging.api.InternalLoggerBinder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-10-25.
 * Copyright 2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceLoader.class, FrameworkProviderService.class, InternalLoggerBinder.class})
public class FrameworkProviderServiceIntegrationTest {

    private ServiceLoader mockServiceLoader;
    private FrameworkProvider mockFrameworkProvider;
    private static Logger mockLogger = PowerMockito.mock(Logger.class);

    @BeforeClass
    public static void setupClass() {
        PowerMockito.mockStatic(InternalLoggerBinder.class, invocationOnMock -> {
            if (Objects.equals(invocationOnMock.getMethod().getName(), "getLogger")) {
                return mockLogger;
            }
            return invocationOnMock.callRealMethod();
        });
    }

    @Before
    public void setUpTest() throws Exception {
        PowerMockito.mockStatic(ServiceLoader.class, invocationOnMock -> {
            if (Objects.equals(invocationOnMock.getMethod().getName(), "load")) {
                return mockServiceLoader;
            } else return invocationOnMock.callRealMethod();
        });
        PowerMockito.spy(FrameworkProviderService.class);
        mockServiceLoader = PowerMockito.mock(ServiceLoader.class);
        mockFrameworkProvider = PowerMockito.mock(FrameworkProvider.class);
    }

    @Test
    public void loadProvidersHasOneTest() throws Exception {
        Mockito.when(mockServiceLoader.iterator()).thenReturn(new Iterator() {

            boolean returned;

            @Override
            public boolean hasNext() {
                return !returned;
            }

            @Override
            public Object next() {
                returned = true;
                return mockFrameworkProvider;
            }
        });
        FrameworkProviderService.loadProviders(null, null);

        Mockito.verify(mockFrameworkProvider, Mockito.times(1)).provide(Mockito.any());
    }

    @Test
    public void loadProvidersHasNoneTest() throws Exception {
        Mockito.when(mockServiceLoader.iterator()).thenReturn(new Iterator() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Object next() {
                return mockFrameworkProvider;
            }
        });
        FrameworkProviderService.loadProviders(null, null);

        Mockito.verify(mockFrameworkProvider, Mockito.times(0)).provide(Mockito.any());
    }

    @Test
    public void loadProvidersConfigurationErrorTest() throws Exception {
        PowerMockito.mockStatic(ServiceLoader.class, invocationOnMock -> {
            if (Objects.equals(invocationOnMock.getMethod().getName(), "load")) {
                throw new ServiceConfigurationError("mock error");
            }
            return invocationOnMock.callRealMethod();
        });
        Mockito.when(mockServiceLoader.iterator()).thenReturn(new Iterator() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Object next() {
                return mockFrameworkProvider;
            }
        });
        FrameworkProviderService.loadProviders(null, null);

        Mockito.verify(mockFrameworkProvider, Mockito.times(0)).provide(Mockito.any());
        Mockito.verify(mockLogger, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(ServiceConfigurationError.class));
    }

}