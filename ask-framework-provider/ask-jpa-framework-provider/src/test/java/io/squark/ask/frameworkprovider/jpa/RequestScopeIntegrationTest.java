package io.squark.ask.frameworkprovider.jpa;

import com.jayway.restassured.http.ContentType;
import io.squark.ask.core.api.Constants;
import io.squark.ask.core.api.exception.AskException;
import io.squark.ask.core.api.logging.LogHelper;
import io.squark.ask.core.api.model.AskConfiguration;
import io.squark.ask.frameworkprovider.ResteasyFrameworkProvider;
import io.squark.ask.frameworkprovider.WeldFrameworkProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
public class RequestScopeIntegrationTest {

    private static EntityManager entityManager;

    @BeforeClass
    public static void setup() throws AskException {
        System.setProperty(Constants.ASK_LOG_LEVEL, "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        AskConfiguration configuration = new AskConfiguration();
        LogHelper.initiateLogging(configuration, RequestScopeIntegrationTest.class.getClassLoader(), null, true);
        new JpaFrameworkProvider().provide(configuration);
        System.setProperty("org.jboss.logging.provider", "slf4j");
        Weld weld = new Weld();
        weld.setClassLoader(WeldFrameworkProvider.class.getClassLoader());
        WeldContainer container = weld.initialize();
        new ResteasyFrameworkProvider().provide(configuration);
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
