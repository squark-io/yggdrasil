package io.squark.yggdrasil.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import java.io.File

/**
 * yggdrasil
 *
 * Created by Erik Håkansson on 2017-03-25.
 * Copyright 2017
 *
 */
class YggdrasilPlugin : Plugin<Project> {

  override fun apply(project: Project)
  {
    project.plugins.apply(JavaPlugin::class.java)

    val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)

    val stageDir: File = File(project.buildDir, YGGDRASIL_STAGE_DIR)
    stageDir.mkdirs()

    project.tasks.withType(PrepareFiles::class.java).whenTaskAdded({
      val prepareFiles: PrepareFiles = it
      val output = javaConvention.sourceSets.findByName("main").output
      prepareFiles.classesDir = output.classesDir
      prepareFiles.resourcesDir = output.resourcesDir
      prepareFiles.stageDir = stageDir
    })

    val prepareFiles: Task = project.tasks.create(YGGDRASIL_PREPARE_FILES, PrepareFiles::class.java)
    prepareFiles.description = YGGDRASIL_PREPARE_FILES_DESC
    prepareFiles.group = YGGDRASIL_GROUP
    prepareFiles.dependsOn(project.tasks.findByPath("classes"), project.tasks.findByPath("processResources"))

    val yggdrasil = project.tasks.create(YGGDRASIL_TASK, Jar::class.java, {
      it.description = YGGDRASIL_TASK_DESC
      it.group = YGGDRASIL_GROUP
      it.baseName += "-yggdrasil"
      it.dependsOn(prepareFiles)
      it.manifest.attributes["Main-Class"] = "io.squark.yggdrasil.bootstrap.Yggdrasil"
      it.from(stageDir)
    })

    project.tasks.getByName("test").dependsOn(yggdrasil)
  }

  companion object {
    internal val YGGDRASIL_GROUP = "build"
    internal val YGGDRASIL_PREPARE_FILES = "yggdrasilPrepareFiles"
    internal val YGGDRASIL_PREPARE_FILES_DESC = "Prepare files for the Yggdrasil JAR"
    internal val YGGDRASIL_TASK = "yggdrasil"
    internal val YGGDRASIL_TASK_DESC = "Assembles the Yggdrasil JAR"
    internal val YGGDRASIL_STAGE_DIR = "/tmp/yggdrasil-stage"
  }
}
