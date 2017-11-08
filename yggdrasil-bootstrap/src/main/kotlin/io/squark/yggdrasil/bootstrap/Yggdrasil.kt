package io.squark.yggdrasil.bootstrap

import io.squark.yggdrasil.bootstrap.Yggdrasil.Companion.initialize
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader

/**
 * Main Yggdrasil initiation class. Always use this, either as Main-Class or through
 * [initialize] to instantiate an Yggdrasil application
 *
 * Created by Erik Håkansson on 2017-04-08.
 * Copyright 2017
 */
class Yggdrasil {

  /**
   * @suppress
   */
  companion object {

    private const val yggdrasilInternalClassName = "io.squark.yggdrasil.core.YggdrasilInternal"

    /**
     * Standard main method. If manifest contains Delegated-Main-Class, the designated Class will be loaded after
     * Yggdrasil initialization and args passed to it.
     *
     * @param args Array of strings to be passed to a delegated Main-Class
     */
    @JvmStatic fun main(args: Array<String>?) {
      initialize(args)
    }

    /**
     * Main initialization method. Use this when initializing Yggdrasil manually.
     * Method should *not* be used in conjunction with Delegated-Main-Class in Manifest as that may lead to a never ending loop of initializations.
     * If method is used directly instead of through Main-Class [args] is not used.
     *
     * @param args unused
     */
    @JvmStatic fun initialize(args: Array<String>?) {
      val parentClassLoader = this::class.java.classLoader as URLClassLoader
      val classLoader = YggdrasilClassLoader(parentClassLoader, parentClassLoader.urLs)
      Thread.currentThread().contextClassLoader = classLoader
      val yggdrasilClass = classLoader.loadClass(yggdrasilInternalClassName)
      val instance = yggdrasilClass.newInstance()
      val _initialize = yggdrasilClass.getDeclaredMethod("_initialize", Array<String>::class.java, Map::class.java)
      _initialize.isAccessible = true
      var message = ""
      var i = 0
      if (classLoader.duplicates.filterValues { it.size > 1 }.isNotEmpty()) {
        message += "Found duplicated class(es). Only the first defined will be loaded."
      }
      for (duplicate in classLoader.duplicates.filterValues { it.size > 1 }) {
        if (i > 20) {
          message += "\nand ${classLoader.duplicates.size - 20} more..."
          break
        }
        message += "\n${duplicate.key} is duplicated in ${duplicate.value}"
        i++
      }
      try {
        _initialize.invoke(instance, args, mapOf(Pair("TRACE", listOf(message))))
      } catch (e: InvocationTargetException) {
        throw RuntimeException(e)
      }
    }
  }
}
