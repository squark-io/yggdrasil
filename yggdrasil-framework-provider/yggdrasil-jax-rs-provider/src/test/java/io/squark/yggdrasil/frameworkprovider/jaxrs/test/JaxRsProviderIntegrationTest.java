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
package io.squark.yggdrasil.frameworkprovider.jaxrs.test;


import com.jayway.restassured.RestAssured;
import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.logging.LogHelper;
import io.squark.yggdrasil.core.api.model.ProviderConfiguration;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.frameworkprovider.JaxRsProvider;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class JaxRsProviderIntegrationTest {

    private static int port;

    @BeforeClass
    public static void setUp() throws Exception {
        port = new Random().nextInt(8999) + 1000;

        System.setProperty(Constants.YGGDRASIL_LOG_LEVEL, "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        YggdrasilConfiguration configuration = new YggdrasilConfiguration();
        Set<ProviderConfiguration> config = new HashSet<>();
        ProviderConfiguration jaxRsConfig = new ProviderConfiguration();
        jaxRsConfig.setIdentifier(JaxRsProvider.class.getSimpleName());
        Map<String, Object> jaxRsProperties = new HashMap<>();
        jaxRsProperties.put("port", String.valueOf(port));
        jaxRsConfig.setProperties(jaxRsProperties);
        config.add(jaxRsConfig);
        configuration.setProviderConfigurations(config);
        LogHelper.initiateLogging(configuration, JaxRsProviderIntegrationTest.class.getClassLoader(), null, true);
        System.setProperty("org.jboss.logging.provider", "slf4j");
        new JaxRsProvider().provide(configuration);
    }

    @Test
    public void testIntegrationTest() {
        RestAssured.given().port(port).get("/").then().assertThat().body("get(0)", Matchers.equalTo("test this string"));
    }

}