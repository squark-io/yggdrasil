package io.squark.yggdrasil.gradle

import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.Manifest

/**
 * Yggdrasil Gradle Task
 *
 * Task for creating Yggdrasil applications conforming to the microprofile.io standard
 * @see <a href="http://microprofile.io">microprofile.io</a>
 *
 * Created by Erik Håkansson on 2017-11-26.
 * Copyright 2017
 *
 */
open class YggdrasilTask : Jar() {

  /**
   * Staging dir for preparing jar
   */
  @OutputDirectory
  fun getStageDir(): File {
    val stageDir = File(project.buildDir, YGGDRASIL_STAGE_DIR)
    stageDir.mkdirs()
    return stageDir
  }

  /**
   * Classes dirs
   */
  @InputFiles
  fun getClassesDirs(): FileCollection {
    val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
    val mainSourceSet = javaConvention.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!!
    return mainSourceSet.output.classesDirs
  }

  /**
   * Resources dir
   */
  @InputDirectory
  fun getResourcesDir(): File {
    val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)

    val mainSourceSet = javaConvention.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)!!
    return mainSourceSet.output.resourcesDir
  }

  /**
   * Actual @TaskAction method
   */
  @TaskAction
  fun action() {
    description = YGGDRASIL_TASK_DESC
    group = YGGDRASIL_GROUP
    classifier = "yggdrasil"

    val resourcesDir = getResourcesDir()
    val classesDirs = getClassesDirs()

    from(getStageDir())
    val beansXML = File(getStageDir(), "META-INF/beans.xml")
    if (!beansXML.exists()) {
      beansXML.parentFile.mkdirs()
      beansXML.createNewFile()
      val standin = this@YggdrasilTask.javaClass.getResourceAsStream(
        "/META-INF/standins/empty-beans.xml") ?: throw GradleException(
        "Failed to find /META-INF/standins/empty-beans.xml")
      val bytes = IOUtils.toByteArray(standin)
      beansXML.writeBytes(bytes)
    }

    val bootstrapFirstLevel = project.configurations.getAt(
      "yggdrasil-bootstrap").resolvedConfiguration.firstLevelModuleDependencies.filter {
      it.moduleGroup == "io.squark.yggdrasil" && it.moduleName == "yggdrasil-bootstrap"
    }
    if (bootstrapFirstLevel.size != 1) {
      throw GradleException(
        "Failed to find exactly 1 occurence of yggdrasil-bootstrap dependency. Found ${bootstrapFirstLevel.size}")
    }
    val bootstrapArtifacts = bootstrapFirstLevel.single().allModuleArtifacts
    bootstrapArtifacts.forEach { unpackJar(it.file, getStageDir()) }

    val dependencies = getProjectDependencies(project)
    project.copy {
      from(dependencies)
      into(File(getStageDir(), "META-INF/libs"))
    }

    val files = getProjectFiles(classesDirs, resourcesDir, project)
    project.copy {
      from(files)
      into(getStageDir())
    }
    val manifestFile = File(resourcesDir, JarFile.MANIFEST_NAME)
    var delegatedMainClass: String? = null
    if (manifestFile.exists()) {
      val fileInputStream = FileInputStream(manifestFile)
      val manifest = Manifest(fileInputStream)
      fileInputStream.close()
      if (manifest.mainAttributes[Attributes.Name.MAIN_CLASS] != null) {
        delegatedMainClass = manifest.mainAttributes[Attributes.Name.MAIN_CLASS].toString()
      }
    }

    manifest.attributes[Attributes.Name.MAIN_CLASS.toString()] = YGGDRASIL_MAIN_CLASS
    when {
      delegatedMainClass != null -> manifest.attributes[DELEGATED_MAIN_CLASS] = delegatedMainClass
    }
    if (!manifest.attributes.containsKey(Attributes.Name.MANIFEST_VERSION.toString())) {
      manifest.attributes[Attributes.Name.MANIFEST_VERSION.toString()] = "1.0"
    }
    if (!manifest.attributes.containsKey("Build-Jdk")) {
      manifest.attributes["Build-Jdk"] = System.getProperty("java.version")
    }
    copy()
  }

  private fun unpackJar(file: File, stageDir: File) {
    val jarFile = JarFile(file)
    jarFile.entries().iterator().forEach {
      when {
        !it.isDirectory -> {
          val targetFile = File(stageDir, it.name)
          when {
            targetFile.exists() -> when {
              it.name != "META-INF/MANIFEST.MF" -> logger.warn("Duplicate file ${it.name}. Skipping from ${file}")
            }
            else -> {
              targetFile.parentFile.mkdirs()
              targetFile.createNewFile()
              val outputStream = FileOutputStream(targetFile)
              val inputStream = jarFile.getInputStream(it)
              IOUtils.copy(inputStream, outputStream)
              outputStream.close()
              inputStream.close()
            }
          }
        }
      }
    }
  }

  private fun getProjectDependencies(project: Project): FileCollection {
    return project.files(project.configurations.getAt("compile"))
  }

  private fun getProjectFiles(classesDirs: FileCollection, resourcesDir: File?, project: Project): FileCollection {
    val files = mutableSetOf<FileTree>()
    files.add(classesDirs.asFileTree)
    if (resourcesDir != null && resourcesDir.exists())
      files.add(project.fileTree(resourcesDir))

    return project.files(files.toTypedArray())
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
        val output = File(stageDir, "META-INF/libs/$entryPath")
        output.parentFile.mkdirs()
        output.createNewFile()
        val outputStream = FileOutputStream(output)
        IOUtils.copy(inputStream, outputStream)
        outputStream.close()
      }
      inputStream.close()
    }
  }

  internal companion object {
    private const val DELEGATED_MAIN_CLASS = "Delegated-Main-Class"
    private const val YGGDRASIL_MAIN_CLASS = "io.squark.yggdrasil.bootstrap.Yggdrasil"
    private const val YGGDRASIL_GROUP = "build"
    private const val YGGDRASIL_TASK_DESC = "Assembles the Yggdrasil JAR"
    private const val YGGDRASIL_STAGE_DIR = "/tmp/yggdrasil-stage"
  }
}
