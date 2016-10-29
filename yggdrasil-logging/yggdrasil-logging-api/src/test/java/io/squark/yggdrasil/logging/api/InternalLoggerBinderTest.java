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
package io.squark.yggdrasil.logging.api;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-10-29.
 * Copyright 2016
 */
public class InternalLoggerBinderTest {
    @Test
    public void getSingleton() throws Exception {
        Assert.assertNotNull(InternalLoggerBinder.getSingleton());
    }

    @Test
    public void getLogger() throws Exception {
        Assert.assertEquals(InternalLoggerBinderTest.class.getName(),
            InternalLoggerBinder.getLogger(InternalLoggerBinderTest.class).getName());
        Assert.assertEquals(InternalLoggerBinderTest.class.getName(),
            InternalLoggerBinder.getLogger(InternalLoggerBinderTest.class.getName()).getName());
    }

    @Test
    public void getLoggerFactory() throws Exception {
        Assert.assertEquals(CrappyLoggerFactory.class, InternalLoggerBinder.getSingleton().getLoggerFactory().getClass());
        InternalLoggerBinder.getSingleton().notifyLoggingInitialized(null);
        Assert.assertNull(InternalLoggerBinder.getSingleton().getLoggerFactory());

        //Reset static instance so as not to break other tests:
        Field instance = InternalLoggerBinder.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        Constructor<InternalLoggerBinder> constructor = InternalLoggerBinder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        instance.set(null, constructor.newInstance());
    }

    @Test
    public void register() throws Exception {
        CrappyLogger logger = new CrappyLogger(InternalLoggerBinderTest.class.getName());
        InternalLoggerBinder.getSingleton().register(logger);
        Field loggers = InternalLoggerBinder.class.getDeclaredField("loggers");
        loggers.setAccessible(true);
        @SuppressWarnings("unchecked") Set<CrappyLogger> loggerSet =
            (Set<CrappyLogger>) loggers.get(InternalLoggerBinder.getSingleton());
        Assert.assertEquals(1, loggerSet.size());
        Assert.assertEquals(logger, loggerSet.iterator().next());

    }

    @Test
    public void notifyLoggingInitialized() throws Exception {

    }

}