package io.squark.yggdrasil.core.cdi

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.inject.spi.Extension
import javax.enterprise.inject.spi.ProcessAnnotatedType

/**
 * CDI Extension to blacklist classes belonging to certain packages.
 *
 * Created by Erik Håkansson on 2017-04-05.
 * Copyright 2017
 */
@ApplicationScoped class ClassBlacklistObserver : Extension {

  private val blacklistedPackages = listOf("org\\.apache\\.logging\\.log4j.*")

  /**
   * Method handling actual CDI observation
   */
  fun <T> observeClasses(@Observes event: ProcessAnnotatedType<T>) {
    if (blacklistedPackages
      .filter { event.annotatedType.javaClass.name.matches(it.toRegex()) }
      .isNotEmpty()) {
      event.veto()
    }
  }
}
