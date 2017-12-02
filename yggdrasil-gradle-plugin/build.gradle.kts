import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
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

  val jarTask = "jar"(Jar::class) {
    manifest {
      attributes["Implementation-Version"] = version
    }
  }

  val itPrepare by creating {
    val propFile = File("$projectDir/test/gradle.properties")
    inputs.files(jarTask.outputs.files)
    outputs.file(propFile)
    doFirst {
      val prop = Properties()
      prop.load(FileInputStream(propFile))
      prop["version"] = version
      propFile.writeText(prop.entries.joinToString(separator = "\n", transform = { "${it.key}=${it.value}" }))
    }
    dependsOn(jarTask)
  }
  val publishToMavenLocal = "publishToMavenLocal" {
    outputs.dirs("$projectDir/test/build/test-results")
  }
  val it by creating(GradleBuild::class) {
    inputs.files(jarTask.outputs.files, "$projectDir/test/src")
    outputs.dirs("$projectDir/test/build/test-results")
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
  compile(kotlin("stdlib", dependencyVersions["kotlin"]))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")
  provided(project(":yggdrasil-core"))
  provided(project(":yggdrasil-bootstrap"))
}
