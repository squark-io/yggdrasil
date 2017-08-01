package io.squark.yggdrasil.bootstrap

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

/**
 * Helper class for handling nested jar streams
 */
class NestedJarStreamHandler : URLStreamHandler() {
  /**
   * @see java.net.URLStreamHandler.openConnection
   */
  override fun openConnection(url: URL): URLConnection {
    val connection = NestedJarURLConnection(url)
    connection.connect()
    return connection
  }
}