package io.squark.yggdrasil.core.jaxrs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr353.JSR353Module
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider
import javax.ws.rs.ext.Provider

/**
 * JsonPProvider for Jackson2
 *
 * Created by Erik Håkansson on 2017-04-02.
 * Copyright 2017
 */
@Provider open class JsonPProvider : ResteasyJackson2Provider() {

  private val mapper = ObjectMapper()

  init {
    mapper.registerModule(JSR353Module())
    @Suppress("LeakingThis")
    setMapper(mapper)
  }

}
