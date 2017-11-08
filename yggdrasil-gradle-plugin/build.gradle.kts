import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.io.FileInputStream
import java.util.Properties

val dependencyVersions: Map<String, String> by extra

description = "Yggdrasil Gradle Plugin"

plugins {
  `kotlin-dsl`
}

tasks {
  "clean" {
    doFirst {
      delete("$projectDir/test/build")
    }
  }

  val coreConfig = configurations.maybeCreate("yggdrasil-core")
  val bootstrapConfig = configurations.maybeCreate("yggdrasil-bootstrap")
  java.sourceSets.create("yggdrasil-core")
  java.sourceSets.create("yggdrasil-bootstrap")
  dependencies.add("yggdrasil-core", project(":yggdrasil-core"))
  dependencies.add("yggdrasil-bootstrap", project(":yggdrasil-bootstrap"))
  val resourcesDir = java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.resourcesDir
  val copyCore by creating(Copy::class) {
    from(project.files(coreConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-core"))
  }
  val copyBootstrap by creating(Copy::class) {
    from(project.files(bootstrapConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-bootstrap"))
  }
  val jar = "jar" {
    dependsOn(copyCore, copyBootstrap)
  }

  val itPrepare by creating {
    val propFile = File("$projectDir/test/gradle.properties")
    inputs.files(jar.outputs.files)
    outputs.file(propFile)
    doFirst {
      val prop = Properties()
      prop.load(FileInputStream(propFile))
      prop["version"] = version
      propFile.writeText(prop.entries.joinToString(separator = "\n", transform = { "${it.key}=${it.value}" }))
    }
    dependsOn(jar)
  }
  val publishToMavenLocal = "publishToMavenLocal" {
    outputs.files("$projectDir/test/build/test-results")
  }
  val it by creating(GradleBuild::class) {
    inputs.files(jar.outputs.files, "$projectDir/test/src")
    outputs.files("$projectDir/test/build/test-results")
    setBuildFile("test/build.gradle.kts")
    startParameter.projectProperties["version"] = version as String
    tasks = listOf("clean", "yggdrasil", "test")
    dependsOn(publishToMavenLocal, itPrepare)
  }
  "test"(Test::class) {
    isScanForTestClasses = false
    dependsOn(it)
  }
}

dependencies {
  compile(kotlin("stdlib"))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")
  provided(project(":yggdrasil-core"))
  provided(project(":yggdrasil-bootstrap"))
}
