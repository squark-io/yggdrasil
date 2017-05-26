import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.GradleBuild
import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.java
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.project
import org.gradle.script.lang.kotlin.repositories
import java.io.File
import java.io.FileInputStream
import java.util.Properties

val dependencyVersions: Map<String, String> by extra

buildscript {
  val dependencyVersions: Map<String, String> by extra
  repositories {
    gradleScriptKotlin()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin", dependencyVersions["kotlin"]))
  }
}

apply {
  plugin("kotlin")
  plugin("maven")
  plugin("java")
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
  val resourcesDir = java.sourceSets.findByName("main").output.resourcesDir
  val copyCore by creating(Copy::class) {
    from(project.files(coreConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-core"))
  }
  val copyBootstrap by creating(Copy::class) {
    from(project.files(bootstrapConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-bootstrap"))
  }
  "jar" {
    dependsOn(copyCore, copyBootstrap)
  }
  val itPrepare by creating {
    val prop = Properties()
    val propFile = File("$projectDir/test/gradle.properties")
    prop.load(FileInputStream(propFile))
    prop["version"] = version
    propFile.writeText(prop.entries.joinToString(separator = "\n", transform = { "${it.key}=${it.value}" }))
  }
  val it by creating(GradleBuild::class) {
    setBuildFile("test/build.gradle.kts")
    startParameter.projectProperties["version"] = version as String
    tasks = listOf("yggdrasil", "test")
    dependsOn("install", itPrepare)
  }
  "test" {
    finalizedBy(it)
  }
}

dependencies {
  compile(gradleApi())
  compile(kotlinModule("stdlib", dependencyVersions["kotlin"]))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")
  compileOnly(project(":yggdrasil-core"))
  compileOnly(project(":yggdrasil-bootstrap"))
}
