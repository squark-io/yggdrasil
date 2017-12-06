import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import java.io.File
import java.io.FileInputStream
import java.util.Properties

description = "Yggdrasil Gradle Plugin"

plugins {
  `kotlin-dsl`
}

tasks {
  "clean" {
    doFirst {
      delete("$projectDir/yggdrasil-gradle-plugin-test/build")
    }
  }
  val jarTask = "jar"(Jar::class) {
    manifest {
      attributes.put("Implementation-Version", version)
    }
  }
  val publishToMavenLocal = "publishToMavenLocal" {
    dependsOn(":yggdrasil-core:publishToMavenLocal", ":yggdrasil-bootstrap:publishToMavenLocal")
  }
  val it by creating(GradleBuild::class) {
    inputs.files(jarTask.outputs.files, "$projectDir/yggdrasil-gradle-plugin-test/src")
    val testResults = File(buildDir, "test-results/")
    outputs.dirs(testResults)
    setBuildFile("$projectDir/yggdrasil-gradle-plugin-test/build.gradle.kts")
    tasks = listOf("clean", "yggdrasil", "test")
    dependsOn(publishToMavenLocal, jarTask)
    doLast {
      copy {
        if (testResults.exists()) {
          testResults.deleteRecursively()
        }
        from(File(projectDir, "yggdrasil-gradle-plugin-test/build/test-results/"))
        into(testResults)
      }
    }
  }
  "test"(Test::class) {
    isScanForTestClasses = false
    dependsOn(it)
  }
}

dependencies {
  compile(kotlin("stdlib"))
  compile("commons-io:commons-io")
  provided(project(":yggdrasil-core"))
  provided(project(":yggdrasil-bootstrap"))
}
