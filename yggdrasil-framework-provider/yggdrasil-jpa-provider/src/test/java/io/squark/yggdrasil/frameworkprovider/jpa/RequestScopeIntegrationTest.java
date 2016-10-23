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
import io.squark.yggdrasil.core.api.model.YggdrasilConfiguration;
import io.squark.yggdrasil.frameworkprovider.JaxRsProvider;
import io.squark.yggdrasil.frameworkprovider.CDIProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class RequestScopeIntegrationTest {

    private static EntityManager entityManager;

    @BeforeClass
    public static void setup() throws YggdrasilException {
        System.setProperty(Constants.YGGDRASIL_LOG_LEVEL, "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        YggdrasilConfiguration configuration = new YggdrasilConfiguration();
        LogHelper.initiateLogging(configuration, RequestScopeIntegrationTest.class.getClassLoader(), null, true);
        new JpaProvider().provide(configuration);
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        weld.setClassLoader(CDIProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        new JaxRsProvider().provide(configuration);
        entityManager = container.select(JpaCDIServices.class).get().getEntityManagerFactoryRef(
                "testPersistenceUnit").getInstance().createEntityManager();
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
        given().contentType(ContentType.JSON).port(8080).get("/").then().assertThat().body("testColumn", equalTo("test value"));
    }
}
