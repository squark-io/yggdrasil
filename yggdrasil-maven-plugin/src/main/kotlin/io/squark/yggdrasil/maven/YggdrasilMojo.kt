package io.squark.yggdrasil.maven

import org.apache.commons.io.IOUtils
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.apache.maven.project.MavenProjectHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

private val EMPTY_BEANS_XML = "META-INF/standins/empty-beans.xml"
private val BEANS_XML = "META-INF/beans.xml"
private val YGGDRASIL_BOOTSTRAP_PATH = "META-INF/yggdrasil-bootstrap/"
private val YGGDRASIL_CORE_PATH = "META-INF/yggdrasil-core/"
private val YGGDRASIL_LIBS_PATH = "META-INF/libs"
private val YGGDRASIL_MAIN_CLASS = "io.squark.yggdrasil.bootstrap.Yggdrasil"

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-03-25.
 * Copyright 2017
 *
 */
@Mojo(name = "yggdrasil")
class YggdrasilMojo : AbstractMojo() {

  /**
   * The MavenProject object.

   * @parameter expression="${project}"
   * *
   * @readonly
   */
  @Parameter(defaultValue = "\${project}", readonly = true, required = true)
  private lateinit var project: MavenProject

  @Component
  private lateinit var mavenProjectHelper: MavenProjectHelper

  var classesDir: File? = null

  val addedResources = mutableListOf<String>()

  override fun execute() {
    classesDir = File(project.build.outputDirectory)
    val manifest = generateAndReturnManifest()
    val blacklist = listOf(YGGDRASIL_BOOTSTRAP_PATH)
    val targetFile = File(project.build.directory, getTargetJarName())
    val targetJar = createTargetJar(targetFile, manifest)
    log.info("Building jar: ${targetFile.absolutePath}")
    targetJar.use {
      addFile(classesDir!!, null, it, blacklist)
      val beansXML = File(classesDir, BEANS_XML)
      if (!beansXML.exists()) {
        addBeansXml(it)
      }
      addFromJar(YGGDRASIL_BOOTSTRAP_PATH, it, true)
      addFromJar(YGGDRASIL_CORE_PATH, it, false, Paths.get(YGGDRASIL_CORE_PATH), YGGDRASIL_LIBS_PATH)
    }
    targetJar.close()
    mavenProjectHelper.attachArtifact(project, project.artifact.type, "yggdrasil", targetFile)
    log.info("Built jar: ${targetFile.absolutePath}")
  }

  private fun addFromJar(jarFile: JarFile, entry: JarEntry, jarOutputStream: JarOutputStream, extract: Boolean,
                         relativeTo: Path? = null, prefix: String? = null) {
    val inputStream = jarFile.getInputStream(entry)
    if (extract) {
      val jarInputStream = JarInputStream(inputStream)
      var subEntry = jarInputStream.nextJarEntry
      while (subEntry != null) {
        log.debug("Writing ${subEntry.name}")
        if (subEntry.isDirectory) {
          subEntry = jarInputStream.nextJarEntry
          continue
        }
        jarOutputStream.putNextEntry(subEntry)
        val buffer = ByteArray(2048)
        var len = jarInputStream.read(buffer)
        while (len > 0) {
          jarOutputStream.write(buffer, 0, len)
          len = jarInputStream.read(buffer)
        }
        jarOutputStream.closeEntry()
        subEntry = jarInputStream.nextJarEntry
      }
      jarInputStream.close()
    } else {
      val name = "${prefix ?: ""}${relativeTo!!.relativize(Paths.get(entry.name))}"
      log.debug("Writing $name")
      val jarEntry = JarEntry(name)
      jarOutputStream.putNextEntry(jarEntry)
      IOUtils.copy(inputStream, jarOutputStream)
      jarOutputStream.closeEntry()
    }
    inputStream.close()
  }

  private fun addFromJar(path: String, jarOutputStream: JarOutputStream, extract: Boolean, relativeTo: Path? = null,
                         prefix: String? = null) {
    val resource = this::class.java.classLoader.getResource(path)
    if (resource.protocol != "jar") {
      throw MojoExecutionException("Failed to find $path in jar")
    }
    val jarUrlConnection = resource.openConnection() as JarURLConnection
    jarUrlConnection.connect()
    val jarFile = jarUrlConnection.jarFile
    if (jarUrlConnection.jarEntry.isDirectory) {
      jarFile.entries().toList().filter { it.name.startsWith(path) && !it.isDirectory }.forEach {
        addFromJar(jarFile, it, jarOutputStream, extract, relativeTo, prefix)
      }
    } else if (!path.endsWith(".jar", true)) {
      throw MojoExecutionException("$path is not a valid jar")
    } else {
      addFromJar(jarFile, jarUrlConnection.jarEntry, jarOutputStream, extract, relativeTo, prefix)
    }
    jarFile.close()
  }


  private fun addBeansXml(jarOutputStream: JarOutputStream) {
    val standin = this::class.java.classLoader.getResourceAsStream(EMPTY_BEANS_XML)
    val jarEntry = JarEntry(BEANS_XML)
    log.debug("Writing ${jarEntry.name}")
    jarOutputStream.putNextEntry(jarEntry)
    IOUtils.copy(standin, jarOutputStream)
    jarOutputStream.closeEntry()
  }

  private fun addFile(file: File, prefix: String?, jarOutputStream: JarOutputStream, blacklist: List<String>) {
    if (file.isDirectory) {
      file.listFiles().forEach { addFile(it, prefix, jarOutputStream, blacklist) }
    } else {
      addEntry(file, prefix, jarOutputStream, blacklist)
    }
  }

  private fun addEntry(file: File, prefix: String?, jarOutputStream: JarOutputStream, blacklist: List<String>) {
    val name = "${prefix ?: ""}${classesDir!!.toPath().relativize(file.toPath())}"
    log.debug("Writing $name")
    blacklist.forEach {
      if (name.startsWith(it)) {
        return
      }
    }
    if (addedResources.contains(name)) {
      return
    }
    addedResources += name
    val jarEntry = JarEntry(name)
    val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    jarEntry.let {
      it.creationTime = attributes.creationTime()
      it.lastAccessTime = attributes.lastAccessTime()
      it.lastModifiedTime = attributes.lastModifiedTime()
    }
    jarOutputStream.putNextEntry(jarEntry)
    IOUtils.copy(file.inputStream(), jarOutputStream)
    jarOutputStream.closeEntry()
  }

  private fun getTargetJarName(): String {
    return project.artifactId + "-" + project.version + "-yggdrasil." + project.packaging
  }

  @Throws(IOException::class)
  private fun createTargetJar(targetFile: File, manifest: Manifest): JarOutputStream {
    return JarOutputStream(FileOutputStream(targetFile), manifest)
  }

  @Throws(MojoExecutionException::class)
  private fun generateAndReturnManifest(): Manifest {
    val manifestFile = File(project.build.outputDirectory, "META-INF/MANIFEST.MF")
    val manifest: Manifest
    if (manifestFile.exists()) {
      try {
        val fileInputStream = FileInputStream(manifestFile)
        manifest = Manifest(fileInputStream)
        fileInputStream.close()
      } catch (e: IOException) {
        log.error(e)
        throw MojoExecutionException(e.message)
      }

    } else {
      manifest = Manifest()
      manifest.mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
      manifest.mainAttributes.put(Attributes.Name("Build-Jdk"), System.getProperty("java.version"))
    }
    manifest.mainAttributes.put(Attributes.Name.MAIN_CLASS, YGGDRASIL_MAIN_CLASS)
    return manifest
  }
}
