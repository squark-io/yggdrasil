package io.hakansson.dynamicjar.frameworkprovider.resteasy.test;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-29.
 * Copyright 2016
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class TestPath {

    @Inject
    Injected injected;

    @GET
    public List<String> testPath() {
        return Collections.singletonList(injected.getString());
    }
}
