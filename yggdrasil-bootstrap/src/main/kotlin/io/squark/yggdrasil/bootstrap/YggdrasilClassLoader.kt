package io.squark.yggdrasil.bootstrap

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Paths
import java.util.Collections
import java.util.Enumeration
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-04-07.
 * Copyright 2017
 *
 */
class YggdrasilClassLoader(val delegate: ClassLoader, urls: Array<URL>) : ClassLoader(null) {

  val resources = mutableMapOf<String, MutableList<URL>>()
  val classBytes = mutableMapOf<String, ByteArray>()
  val duplicates = mutableMapOf<String, MutableList<URL>>()

  init {
    urls.forEach { addURL(it) }
  }

  override fun findClass(name: String): Class<*> {
    if (name == javaClass.name) {
      return delegate.loadClass(name)
    }
    val bytes = classBytes[name] ?: return super.findClass(name)
    definePackageIfNecessary(name)
    return defineClass(name, bytes, 0, bytes.size, null)
  }

  override fun findResources(name: String): Enumeration<URL> {
    val list = resources[name] ?: emptyList<URL>()
    return Collections.enumeration(list)
  }

  override fun findResource(name: String): URL? {
    val list = resources[name] ?: emptyList<URL>()
    return list.firstOrNull()
  }

  private fun duplicate(name: String, url: URL) {
    duplicates.computeIfAbsent(name, { mutableListOf<URL>() }).add(url)
  }

  private fun putResource(name: String, url: URL) {
    resources.computeIfAbsent(name, { mutableListOf<URL>() }).add(url)
  }

  private fun addURL(url: URL) {
    if (url.protocol == "file") {
      addFile(url)
    } else {
      throw NotImplementedError("Protocol ${url.protocol}")
    }
  }

  private fun addFile(url: URL, base: URL? = null) {
    val file = File(url.file)
    if (!file.exists()) throw FileNotFoundException(file.absolutePath)
    val notNullBase = base ?: url
    val relative = Paths.get(notNullBase.toURI()).relativize(Paths.get(url.toURI()))
    if (file.isDirectory) {
      handleDirectory(file, notNullBase)
    } else {
      if (file.extension == "jar") {
        addJar(file)
      } else if (file.extension == "class") {
        addClass(file, relative.toString(), url)
      }
    }
    putResource(relative.toString(), url)
  }

  private fun addJar(file: File) {
    val jarFile = JarFile(file, true)
    for (entry in jarFile.entries()) {
      addEntry(jarFile, entry, file)
    }
    jarFile.close()
  }

  private fun addEntry(jarFile: JarFile, entry: JarEntry, original: File) {
    val url = URL("jar", "", -1, "${original.toURI()}!/${entry.name}",
      NestedJarStreamHandler())
    putResource(entry.name, url)
    if (entry.name.endsWith(".class")) {
      val entryInputStream = jarFile.getInputStream(entry)
      val bytes = entryInputStream.readBytes(entry.size.toInt())
      entryInputStream.close()
      addClass(bytes, entry.name.replace("/", ".").replace("\\.class$".toRegex(), ""), url)
    } else if (entry.name.endsWith(".jar")) {
      addNestedJar(jarFile, entry, original)
    }
  }

  private fun addNestedJar(jarFile: JarFile, entry: JarEntry, original: File) {
    val jarInputStream = JarInputStream(jarFile.getInputStream(entry))
    var subEntry: JarEntry? = jarInputStream.nextJarEntry
    val buffer = ByteArray(2048)
    while (subEntry != null) {
      val url = URL("jar", "", -1, "${original.toURI()}!/${entry.name}!/${subEntry.name}",
        NestedJarStreamHandler())
      putResource(subEntry.name, url)
      if (subEntry.name.endsWith(".class")) {
        val outputStream = ByteArrayOutputStream()
        var len = jarInputStream.read(buffer)
        while (len > 0) {
          outputStream.write(buffer, 0, len)
          len = jarInputStream.read(buffer)
        }
        addClass(outputStream.toByteArray(), subEntry.name.replace("/", ".").replace("\\.class$".toRegex(), ""), url)
        outputStream.close()
      } else if (subEntry.name.endsWith(".jar")) {
        throw NotImplementedError("Twice-nested jar not supported. (Yet. Raise issue if you really need this.)")
      }
      subEntry = jarInputStream.nextJarEntry
    }
    jarInputStream.close()
  }

  private fun addClass(file: File, name: String, source: URL) {
    val bytes = file.readBytes()
    addClass(bytes, name, source)
  }

  private fun addClass(bytes: ByteArray, name: String, source: URL) {
    duplicate(name, source)
    classBytes.putIfAbsent(name, bytes)
  }

  private fun definePackageIfNecessary(className: String) {
    val lastIndex = className.lastIndexOf('.')
    if (lastIndex > 0) {
      val packageName = className.substring(0, lastIndex)
      getPackage(packageName) ?: definePackage(packageName, null, null, null, null, null, null, null)
    }
  }

  private fun handleDirectory(file: File, base: URL) {
    if (!file.isDirectory) {
      throw FileNotFoundException("${file.absolutePath} is not a directory")
    }
    file.listFiles().forEach { addFile(it.toURI().toURL(), base) }
  }
}

