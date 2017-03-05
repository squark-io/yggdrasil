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
package io.squark.yggdrasil.frameworkprovider.jpa;

import com.jayway.restassured.http.ContentType;
import io.squark.yggdrasil.core.api.Constants;
import io.squark.yggdrasil.core.api.exception.YggdrasilException;
import io.squark.yggdrasil.core.api.logging.LogHelper;
import io.squark.yggdrasil.core.api.model.ProviderConfiguration;
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.frameworkprovider.CDIProvider;
import io.squark.yggdrasil.frameworkprovider.ServletDeploymentProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class RequestScopeIntegrationTest {

    private static EntityManager entityManager;
    private static int port;

    @BeforeClass
    public static void setup() throws YggdrasilException {
        port = new Random().nextInt(8999) + 1000;
        Set<ProviderConfiguration> config = new HashSet<>();
        ProviderConfiguration jaxRsConfig = new ProviderConfiguration();
        jaxRsConfig.setIdentifier(ServletDeploymentProvider.class.getSimpleName());
        Map<String, Object> jaxRsProperties = new HashMap<>();
        jaxRsProperties.put("port", String.valueOf(port));
        jaxRsConfig.setProperties(jaxRsProperties);
        config.add(jaxRsConfig);
        System.setProperty(Constants.YGGDRASIL_LOG_LEVEL, "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        YggdrasilConfiguration configuration = new YggdrasilConfiguration();
        configuration.setProviderConfigurations(config);
        LogHelper.initiateLogging(configuration, RequestScopeIntegrationTest.class.getClassLoader(), null, true);
        new JpaProvider().provide(configuration);
        System.setProperty("org.jboss.logging.provider", "slf4j");
        new CDIProvider().provide(configuration);
        new ServletDeploymentProvider().provide(configuration);
        entityManager =
            CDI.current().select(JpaCDIServices.class).get().getEntityManagerFactoryRef("testPersistenceUnit").getInstance()
                .createEntityManager();
    }

    @Test
    public void testStuff() {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setTestColumn("test value");
        entityManager.getTransaction().begin();
        entityManager.persist(sampleEntity);
        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.close();
        given().contentType(ContentType.JSON).port(port).get("/rest").then().assertThat().body("testColumn", equalTo("test value"));
    }
}
