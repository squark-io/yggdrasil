package io.squark.yggdrasil.bootstrap

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.file.Paths
import java.util.Collections
import java.util.Enumeration
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream

/**
 * ClassLoader for Yggdrasil applications
 *
 * Created by Erik Håkansson on 2017-04-07.
 * Copyright 2017
 *
 * @property delegate {@link java.lang.ClassLoader <tt>ClassLoader</tt>} to delegate. Must not be null.
 * @param urls Array of {@link java.net.URL <tt>URL</tt>}s to add to ClassLoader
 * @suppress
 */
class YggdrasilClassLoader(private val delegate: ClassLoader, urls: Array<URL>) : ClassLoader(null) {

  private val resources = mutableMapOf<String, MutableList<URL>>()
  private val classBytes = mutableMapOf<String, ByteArray>()
  internal val duplicates = mutableMapOf<String, MutableList<URL>>()

  init {
    urls.forEach { addURL(it) }
  }

  /**
   * Finds the class with the specified <a href="#name">binary name</a>.
   * This method should be overridden by class loader implementations that
   * follow the delegation model for loading classes, and will be invoked by
   * the [loadClass] method after checking the
   * parent class loader for the requested class.  The default implementation
   * throws a <tt>ClassNotFoundException</tt>.
   *
   * @param  name
   *         The <a href="#name">binary name</a> of the class
   *
   * @return  The resulting <tt>Class</tt> object
   *
   * @throws  ClassNotFoundException
   *          If the class could not be found
   *
   * @since  1.2
   */
  override fun findClass(name: String): Class<*> {
    if (name == javaClass.name) {
      return delegate.loadClass(name)
    }
    val bytes = classBytes[name] ?: return super.findClass(name)
    definePackageIfNecessary(name)
    return defineClass(name, bytes, 0, bytes.size, null)
  }

  /**
   * Returns an enumeration of {@link java.net.URL <tt>URL</tt>} objects
   * representing all the resources with the given name. Class loader
   * implementations should override this method to specify where to load
   * resources from.
   *
   * @param  name
   *         The resource name
   *
   * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
   *          the resources
   *
   * @throws  IOException
   *          If I/O errors occur
   *
   * @since  1.2
   */
  override fun findResources(name: String): Enumeration<URL> {
    val list = resources[name] ?: emptyList<URL>()
    return Collections.enumeration(list)
  }

  /**
   * Finds the resource with the given name. Class loader implementations
   * should override this method to specify where to find resources.
   *
   * @param  name
   *         The resource name
   *
   * @return  A <tt>URL</tt> object for reading the resource, or
   *          <tt>null</tt> if the resource could not be found
   *
   * @since  1.2
   */
  override fun findResource(name: String): URL? {
    val list = resources[name] ?: emptyList<URL>()
    return list.firstOrNull()
  }

  private fun duplicate(name: String, url: URL) {
    duplicates.computeIfAbsent(name, { mutableListOf() }).add(url)
  }

  private fun putResource(name: String, url: URL) {
    resources.computeIfAbsent(name, { mutableListOf() }).add(url)
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

