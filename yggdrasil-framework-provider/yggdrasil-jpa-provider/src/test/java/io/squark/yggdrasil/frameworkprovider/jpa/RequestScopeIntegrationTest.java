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

/**
 * yggdrasil
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
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
