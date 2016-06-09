package io.hakansson.dynamicjar.frameworkprovider.db;

import com.jayway.restassured.http.ContentType;
import io.hakansson.dynamicjar.core.api.Constants;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.logging.LogHelper;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.frameworkprovider.ResteasyFrameworkProvider;
import io.hakansson.dynamicjar.frameworkprovider.WeldFrameworkProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.*;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
@Test
public class RequestScopeIntegrationTest implements ITestClass {

    EntityManager entityManager;

    @BeforeSuite
    public void setup() throws DynamicJarException {
        System.setProperty(Constants.DYNAMICJAR_LOG_LEVEL, "DEBUG");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        DynamicJarConfiguration configuration = new DynamicJarConfiguration();
        LogHelper.initiateLogging(configuration, this.getClass().getClassLoader(), null, true);
        new JpaFrameworkProvider().provide(configuration);
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
