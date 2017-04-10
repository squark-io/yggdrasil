package io.squark.yggdrasil.core.jaxrs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr353.JSR353Module
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider
import javax.ws.rs.ext.Provider

/**
 * yggdrasil
 *
 *
 * Created by Erik Håkansson on 2017-04-02.
 * Copyright 2017
 */
@Provider
open class JsonPProvider : ResteasyJackson2Provider {

  open val mapper = ObjectMapper()

  constructor() {
    mapper.registerModule(JSR353Module())
    setMapper(mapper)
  }
}
