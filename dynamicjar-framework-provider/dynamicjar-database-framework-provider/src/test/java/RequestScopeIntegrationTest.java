import com.jayway.restassured.http.ContentType;
import io.hakansson.dynamicjar.core.api.exception.DynamicJarException;
import io.hakansson.dynamicjar.core.api.model.DynamicJarConfiguration;
import io.hakansson.dynamicjar.frameworkprovider.ResteasyFrameworkProvider;
import io.hakansson.dynamicjar.frameworkprovider.SampleEntity;
import io.hakansson.dynamicjar.frameworkprovider.WeldFrameworkProvider;
import io.hakansson.dynamicjar.frameworkprovider.db.DatabaseFrameworkProvider;
import org.jboss.weld.environment.se.Weld;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
@Test
public class RequestScopeIntegrationTest {

    EntityManager entityManager;

    @BeforeSuite
    public void setup() throws DynamicJarException {
        DynamicJarConfiguration configuration = new DynamicJarConfiguration();
        new DatabaseFrameworkProvider().provide(configuration);
        Weld weld = new Weld();
        weld.setClassLoader(WeldFrameworkProvider.class.getClassLoader());
        weld.initialize();
        new ResteasyFrameworkProvider().provide(configuration);
        entityManager = Persistence.createEntityManagerFactory("testPersistenceUnit").createEntityManager();
    }

    @Test
    public void testStuff() {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setTestColumn("test value");
        entityManager.getTransaction().begin();
        entityManager.persist(sampleEntity);
        entityManager.flush();
        entityManager.getTransaction().commit();
        given().contentType(ContentType.JSON).port(8080).get("/").then().assertThat().body("testColumn", equalTo("test value"));
    }


}
