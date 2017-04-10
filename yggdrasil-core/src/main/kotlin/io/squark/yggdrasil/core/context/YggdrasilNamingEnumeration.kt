package io.squark.yggdrasil.core.context

import javax.naming.NamingEnumeration
import javax.naming.NamingException

/**
 * yggdrasil
 *
 *
 * Created by Erik Håkansson on 2017-04-01.
 * Copyright 2017
 */
class YggdrasilNamingEnumeration<T> constructor(list: Collection<T>) : NamingEnumeration<T> {

  var enumerator: Iterator<T>? = list.iterator()
  var closed = false

  override fun hasMore(): Boolean {
    checkNotClosed()
    return enumerator!!.hasNext()
  }

  override fun close() {
    if (!closed) {
      enumerator = null
      closed = true
    }
  }

  override fun next(): T {
    checkNotClosed()
    return enumerator!!.next()
  }

  override fun hasMoreElements(): Boolean {
    checkNotClosed()
    return enumerator!!.hasNext()
  }

  override fun nextElement(): T {
    checkNotClosed()
    return enumerator!!.next()
  }

  private fun checkNotClosed() {
    if (closed) throw NamingException("Enumeration is closed")
  }
}
