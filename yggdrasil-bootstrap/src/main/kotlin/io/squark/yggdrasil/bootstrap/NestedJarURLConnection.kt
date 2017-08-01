package io.squark.yggdrasil.bootstrap

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream

class NestedJarURLConnection(url: URL) : URLConnection(url), Closeable {

  private var _inputStream: InputStream? = null

  override fun close() {
    _inputStream ?: _inputStream!!.close()
  }

  override fun connect() {
    if (url.protocol != "jar") {
      throw NotImplementedError("Protocol ${url.protocol} not supported")
    }
    val parts = url.path.split("!/")
    if (parts.size < 2 || parts.size > 3) {
      throw IllegalArgumentException(
        "URL should have jar part followed by entry and optional subentry (jar://path/to/jar.jar!/entry/in/jar!/sub/entry). Was \"${url.path}\"")
    }
    val jar = parts[0]
    val entry = parts[1]
    val jarFile = JarFile(File(URL(jar).file))
    val jarEntry = jarFile.getJarEntry(entry)
    if (parts.size == 3) {
      val subEntryName = parts[2]
      if (!jarEntry.name.endsWith(".jar")) {
        throw IllegalArgumentException("Only JAR entries can hold subEntries. Was ${jarEntry.name}")
      }
      val jarInputStream = JarInputStream(jarFile.getInputStream(jarEntry))
      var subEntry: JarEntry? = jarInputStream.nextJarEntry
      var bytes: ByteArray? = null
      while (subEntry != null) {
        if (subEntryName == subEntry.name) {
          val buffer = ByteArray(2048)
          val outputStream = ByteArrayOutputStream()
          var len = jarInputStream.read(buffer)
          while (len > 0) {
            outputStream.write(buffer, 0, len)
            len = jarInputStream.read(buffer)
          }
          bytes = outputStream.toByteArray()
          outputStream.close()
          break
        }
        subEntry = jarInputStream.nextJarEntry
        continue
      }
      if (bytes == null) {
        throw FileNotFoundException("${url}")
      }
      _inputStream = ByteArrayInputStream(bytes)
      jarInputStream.close()
      jarFile.close()
    } else {
      _inputStream = jarFile.getInputStream(jarEntry)
    }
  }

  override fun getInputStream(): InputStream? {
    return _inputStream
  }
}