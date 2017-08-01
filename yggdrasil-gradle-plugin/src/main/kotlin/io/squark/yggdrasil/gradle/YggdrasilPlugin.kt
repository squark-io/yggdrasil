package io.squark.yggdrasil.gradle

import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.java.JavaLibrary
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.invoke
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import kotlin.collections.set
import java.util.jar.Manifest as JdkManifest


/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-03-25.
 * Copyright 2017
 *
 */
class YggdrasilPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.plugins.apply(JavaPlugin::class.java)

    val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)

    val stageDir: File = File(project.buildDir, YGGDRASIL_STAGE_DIR)
    stageDir.mkdirs()

    val mainSourceSet = javaConvention.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)
    val classesDir = mainSourceSet.java.outputDir
    val resourcesDir = mainSourceSet.output.resourcesDir


    project.run {
      tasks {
        YGGDRASIL_TASK(Jar::class) {
          description = YGGDRASIL_TASK_DESC
          group = YGGDRASIL_GROUP
          classifier = "yggdrasil"
          dependsOn("classes")

          inputs.files(classesDir, resourcesDir)

          from(stageDir)
          doFirst {

            val beansXML = File(stageDir, "META-INF/beans.xml")
            if (!beansXML.exists()) {
              beansXML.parentFile.mkdirs()
              beansXML.createNewFile()
              val standin = this@YggdrasilPlugin.javaClass.getResourceAsStream(
                "/META-INF/standins/empty-beans.xml") ?: throw GradleException(
                "Failed to find /META-INF/standins/empty-beans.xml")
              val bytes = IOUtils.toByteArray(standin)
              beansXML.writeBytes(bytes)
            }

            val yggdrasilCoreDir = this@YggdrasilPlugin.javaClass.getResource("/META-INF/yggdrasil-core/")
            if (yggdrasilCoreDir == null || yggdrasilCoreDir.protocol != "jar") {
              throw GradleException("Failed to find META-INF/yggdrasil-core/")
            }
            copyJarPath(yggdrasilCoreDir, stageDir, false)
            val yggdrasilBootstrapDir = this@YggdrasilPlugin.javaClass.getResource("/META-INF/yggdrasil-bootstrap/")
            if (yggdrasilBootstrapDir == null || yggdrasilBootstrapDir.protocol != "jar") {
              throw GradleException("Failed to find META-INF/yggdrasil-bootstrap/")
            }
            copyJarPath(yggdrasilBootstrapDir, stageDir, true)

            val files = getYggdrasilFiles(classesDir, resourcesDir, project)

            project.copy {
              from(files)
              into(stageDir)
            }
            val manifestFile = File(resourcesDir, JarFile.MANIFEST_NAME)
            var delegatedMainClass: String? = null
            if (manifestFile.exists()) {
              val fileInputStream = FileInputStream(manifestFile)
              val manifest = JdkManifest(fileInputStream)
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
          }
        }
      }
    }

    val yggdrasil = project.tasks.findByName(YGGDRASIL_TASK) as Jar
    val jarArtifact = ArchivePublishArtifact(yggdrasil)
    project.extensions.getByType(DefaultArtifactPublicationSet::class.java).addCandidate(jarArtifact)
    project.components.add(JavaLibrary(project.configurations, jarArtifact))

    project.tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(yggdrasil)
  }

  private fun getYggdrasilFiles(classesDir: File?, resourcesDir: File?, project: Project): FileCollection {
    val files = mutableSetOf<FileTree>()
    if (classesDir != null && classesDir.exists())
      files.add(project.fileTree(classesDir))
    if (resourcesDir != null && resourcesDir.exists())
      files.add(project.fileTree(resourcesDir))

    return project.files(files.toTypedArray())
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

  private companion object {
    private const val DELEGATED_MAIN_CLASS = "Delegated-Main-Class"
    private const val YGGDRASIL_MAIN_CLASS = "io.squark.yggdrasil.bootstrap.Yggdrasil"
    private const val YGGDRASIL_GROUP = "build"
    private const val YGGDRASIL_TASK = "yggdrasil"
    private const val YGGDRASIL_TASK_DESC = "Assembles the Yggdrasil JAR"
    private const val YGGDRASIL_STAGE_DIR = "/tmp/yggdrasil-stage"
  }
}
