package io.squark.yggdrasil.gradle

import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-03-31.
 * Copyright 2017
 *
 */
open class PrepareFiles : DefaultTask() {

  @InputFiles @Optional var classesDir: File? = null
  @InputFiles @Optional var resourcesDir: File? = null

  @OutputDirectory var stageDir: File? = null

  @TaskAction
  fun prepareFiles() {

    val files = getYggdrasilFiles()

    project.copy {
      it.from(files)
      it.into(stageDir)
    }
    val beansXML = File(stageDir, "META-INF/beans.xml")
    if (!beansXML.exists()) {
      beansXML.parentFile.mkdirs()
      beansXML.createNewFile()
      val standin = this::class.java.getResourceAsStream("/META-INF/standins/empty-beans.xml")
      val bytes = IOUtils.toByteArray(standin)
      beansXML.writeBytes(bytes)
    }

    val yggdrasilCoreDir = javaClass.getResource("/META-INF/yggdrasil-core/")
    if (yggdrasilCoreDir == null || yggdrasilCoreDir.protocol != "jar") {
      throw UnknownError("Failed to find META-INF/yggdrasil-core/")
    }
    copyJarPath(yggdrasilCoreDir, stageDir!!, false)
    val yggdrasilBootstrapDir = javaClass.getResource("/META-INF/yggdrasil-bootstrap/")
    if (yggdrasilBootstrapDir == null || yggdrasilBootstrapDir.protocol != "jar") {
      throw UnknownError("Failed to find META-INF/yggdrasil-bootstrap/")
    }
    copyJarPath(yggdrasilBootstrapDir, stageDir!!, true)
  }

  private fun copyJarPath(source: URL, stageDir: File, extract: Boolean) {
    assert(source.protocol == "jar")
    val parts = source.file.split("!/")
    assert(parts.size == 2)
    val jar = URL(parts[0]).file
    val jarFile = JarFile(File(jar))
    val jarEntry = jarFile.getJarEntry(parts[1])
    copyJarEntry(jarFile, jarEntry, stageDir, extract)
  }

  private fun copyJarEntry(jarFile: JarFile, jarEntry: JarEntry, stageDir: File, extract: Boolean) {
    val entries: List<JarEntry>
    if (jarEntry.isDirectory) {
      entries = jarFile.entries().toList().filter { it.name.startsWith(jarEntry.name) && !it.isDirectory }
    } else {
      entries = listOf(jarEntry)
    }
    for (entry in entries) {
      val inputStream = jarFile.getInputStream(entry)
      if (extract && entry.name.endsWith(".jar", true)) {
        val jarInputStream = JarInputStream(inputStream)
        var subEntry = jarInputStream.nextJarEntry
        while (subEntry != null) {
          if (subEntry.isDirectory) {
            subEntry = jarInputStream.nextJarEntry
            continue
          }
          val buffer = ByteArray(2048)
          var len = jarInputStream.read(buffer)
          val outputStream = ByteArrayOutputStream()
          while (len > 0) {
            outputStream.write(buffer, 0, len)
            len = jarInputStream.read(buffer)
          }
          val output = File(stageDir, subEntry.name)
          output.parentFile.mkdirs()
          output.createNewFile()
          val fileOutputStream = FileOutputStream(output)
          IOUtils.write(outputStream.toByteArray(), fileOutputStream)
          outputStream.close()
          fileOutputStream.close()
          subEntry = jarInputStream.nextJarEntry
        }
        jarInputStream.close()
      } else {
        val entryPath = Paths.get("META-INF/yggdrasil-core").relativize(Paths.get(entry.name))
        val output = File(stageDir, "META-INF/libs/${entryPath}")
        output.parentFile.mkdirs()
        output.createNewFile()
        val outputStream = FileOutputStream(output)
        IOUtils.copy(inputStream, outputStream)
        outputStream.close()
      }
      inputStream.close()
    }
  }

  private fun getYggdrasilFiles(): FileCollection {
    val files = mutableSetOf<FileTree>()
    if (classesDir != null && classesDir!!.exists())
      files.add(project.fileTree(classesDir))
    if (resourcesDir != null && resourcesDir!!.exists())
      files.add(project.fileTree(resourcesDir))

    return project.files(files.toTypedArray())
  }

}
