package io.squark.yggdrasil.core.cdi

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.inject.spi.Extension
import javax.enterprise.inject.spi.ProcessAnnotatedType

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-05.
 * Copyright 2017
 *
 */
@ApplicationScoped class ClassBlacklistObserver : Extension {

  private val blacklistedPackages = listOf("org\\.apache\\.logging\\.log4j.*")

  fun <T> observeClasses(@Observes event: ProcessAnnotatedType<T>) {
    if (blacklistedPackages
      .filter { event.annotatedType.javaClass.name.matches(it.toRegex()) }
      .isNotEmpty()) {
      event.veto()
    }
  }
}
