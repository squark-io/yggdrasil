package io.squark.yggdrasil.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.FileInputStream
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest as JdkManifest

private const val DELEGATED_MAIN_CLASS = "Delegated-Main-Class"
private const val YGGDRASIL_MAIN_CLASS = "io.squark.yggdrasil.bootstrap.Yggdrasil"
private const val YGGDRASIL_GROUP = "build"
private const val YGGDRASIL_PREPARE_FILES = "yggdrasilPrepareFiles"
private const val YGGDRASIL_PREPARE_FILES_DESC = "Prepare files for the Yggdrasil JAR"
private const val YGGDRASIL_TASK = "yggdrasil"
private const val YGGDRASIL_TASK_DESC = "Assembles the Yggdrasil JAR"
private const val YGGDRASIL_STAGE_DIR = "/tmp/yggdrasil-stage"

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

    project.tasks.withType(PrepareFiles::class.java).whenTaskAdded({
      val output = javaConvention.sourceSets.findByName("main").output
      it.classesDir = output.classesDir
      it.resourcesDir = output.resourcesDir
      it.stageDir = stageDir
    })

    val prepareFiles: Task = project.tasks.create(YGGDRASIL_PREPARE_FILES, PrepareFiles::class.java, {
      it.description = YGGDRASIL_PREPARE_FILES_DESC
      it.group = YGGDRASIL_GROUP
      it.dependsOn("classes", "processResources")
    })

    val yggdrasil = project.tasks.create(YGGDRASIL_TASK, Jar::class.java, {
      it.description = YGGDRASIL_TASK_DESC
      it.group = YGGDRASIL_GROUP
      it.classifier = "yggdrasil"
      it.dependsOn(prepareFiles)
      it.from(stageDir)
      it.doFirst {
        val output = javaConvention.sourceSets.findByName("main").output
        val manifestFile = File(output.resourcesDir, JarFile.MANIFEST_NAME)
        var delegatedMainClass: String? = null
        if (manifestFile.exists()) {
          val fileInputStream = FileInputStream(manifestFile)
          val manifest = JdkManifest(fileInputStream)
          fileInputStream.close()
          if (manifest.mainAttributes[Attributes.Name.MAIN_CLASS] != null) {
            delegatedMainClass = manifest.mainAttributes[Attributes.Name.MAIN_CLASS].toString()
          }
        }
        (it as Jar).manifest {
          manifest: Manifest ->
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
    })

    project.tasks.getByName("test").dependsOn(yggdrasil)
  }
}
