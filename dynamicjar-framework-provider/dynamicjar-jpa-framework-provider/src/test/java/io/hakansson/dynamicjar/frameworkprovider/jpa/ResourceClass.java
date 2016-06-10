package io.hakansson.dynamicjar.frameworkprovider.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-06-04.
 * Copyright 2016
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ResourceClass {

    @PersistenceContext(unitName = "testPersistenceUnit")
    private EntityManager entityManager;

    @GET
    public SampleEntity get() {
        return (SampleEntity) entityManager.createQuery("from SampleEntity").getSingleResult();
    }
}
