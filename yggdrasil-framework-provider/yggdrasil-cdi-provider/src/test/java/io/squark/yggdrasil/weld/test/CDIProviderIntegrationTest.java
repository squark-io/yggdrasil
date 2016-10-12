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
package io.squark.yggdrasil.weld.test;

import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.logging.LogHelper;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.frameworkprovider.CDIProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;

public class CDIProviderIntegrationTest {

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty(Constants.YGGDRASIL_LOG_LEVEL, "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        YggdrasilConfiguration configuration = new YggdrasilConfiguration();
        LogHelper.initiateLogging(configuration, CDIProviderIntegrationTest.class.getClassLoader(), null, true);
        System.setProperty("org.jboss.logging.provider", "slf4j");
        new CDIProvider().provide(configuration);
    }

    @Test
    public void test() {
        Assert.assertEquals(InjectedClass.class.getName() + ": HELLO", CDI.current().select(InjectedClass.class).get().sayHello());
    }



}