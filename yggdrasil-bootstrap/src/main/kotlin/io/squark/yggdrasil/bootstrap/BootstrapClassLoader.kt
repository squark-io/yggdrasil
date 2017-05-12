package io.squark.yggdrasil.bootstrap

import java.net.URL

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-27.
 * Copyright 2017
 *
 */
class BootstrapClassLoader(url: URL, `package`: String, name: String) : ClassLoader(null) {
  init {
    val bytes = url.readBytes()
    definePackage(`package`, null, null, null, null, null, null, null)
    defineClass("$`package`.$name", bytes, 0, bytes.size, null)
  }
}
