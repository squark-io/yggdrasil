package io.squark.yggdrasil.core.context

import java.util.Hashtable
import javax.naming.Context
import javax.naming.spi.InitialContextFactory

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-01.
 * Copyright 2017
 *
 */

class YggdrasilInitialContextFactory : InitialContextFactory {

  private object ContextHolder {
    val context: Context by lazy {
      YggdrasilContext("")
    }
  }


  override fun getInitialContext(environment: Hashtable<*, *>?): Context {
    return ContextHolder.context
  }
}
