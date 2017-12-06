package test

import javax.json.Json
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * yggdrasil
 *
 *
 * Created by Erik Håkansson on 2017-04-01.
 * Copyright 2017
 */
@Path("/")
open class TestResource {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  open fun test(): Response {
    val jsonObject = Json.createObjectBuilder().add("test", "test").build()
    return Response.ok(jsonObject).build()
  }
}
