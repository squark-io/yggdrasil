package io.squark.yggdrasil.core.context

import java.util.Hashtable
import javax.naming.Context
import javax.naming.spi.InitialContextFactory

/**
 * Implementation of InititalContextFactory to provide limited JNDI functionality
 *
 * Created by Erik Håkansson on 2017-04-01.
 * Copyright 2017
 */
internal class YggdrasilInitialContextFactory : InitialContextFactory {

  private object ContextHolder {
    val context: Context by lazy {
      YggdrasilContext("")
    }
  }

  /**
   * Returns a static context. I.e. this violates the Java JNDI definition, so don't expect this method to return a
   * unique context.
   */
  override fun getInitialContext(environment: Hashtable<*, *>?): Context {
    return ContextHolder.context
  }
}
