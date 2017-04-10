package io.squark.yggdrasil.bootstrap

import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-08.
 * Copyright 2017
 *
 */
class Yggdrasil {

  companion object {

    val yggdrasilInternalClassName = "io.squark.yggdrasil.core.YggdrasilInternal"

    @JvmStatic
    fun main(args: Array<String>) {
      initialize(args)
    }

    @JvmStatic
    fun initialize(args: Array<String>?) {
      val parentClassLoader = this::class.java.classLoader as URLClassLoader
      val classLoader = YggdrasilClassLoader(null, parentClassLoader.urLs)
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
