import org.gradle.api.plugins.JavaPluginConvention
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
import java.io.FileOutputStream
import java.util.Properties

buildscript {
  repositories {
    gradleScriptKotlin()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin"))
  }
}

plugins {
  java
}

apply {
  plugin("kotlin")
  plugin("maven")
}

val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
configurations.maybeCreate("yggdrasil-core")
configurations.maybeCreate("yggdrasil-bootstrap")
javaConvention.sourceSets.create("yggdrasil-core")
javaConvention.sourceSets.create("yggdrasil-bootstrap")
dependencies.add("yggdrasil-core", project(":yggdrasil-core"))
dependencies.add("yggdrasil-bootstrap", project(":yggdrasil-bootstrap"))

val resourcesDir = javaConvention.sourceSets.findByName("main").output.resourcesDir
val copyCore = tasks.create("copyCore", Copy::class.java, {
  from(project.files(project.configurations.getByName("yggdrasil-core")))
  into(File(resourcesDir, "META-INF/yggdrasil-core"))
})
val copyBootstrap = tasks.create("copyBootstrap", Copy::class.java, {
  from(project.files(project.configurations.getByName("yggdrasil-bootstrap")))
  into(File(resourcesDir, "META-INF/yggdrasil-bootstrap"))
})
tasks.findByName("jar").dependsOn(copyCore)
tasks.findByName("jar").dependsOn(copyBootstrap)

dependencies {
  compile(gradleApi())
  compile(kotlinModule("stdlib"))
  compile("commons-io:commons-io:2.5")

  compileOnly(project(":yggdrasil-core"))
  compileOnly(project(":yggdrasil-bootstrap"))
}

val itClean = tasks.create("it-clean", GradleBuild::class.java, {
  setBuildFile("test/build.gradle.kts")
  setTasks(listOf("clean"))
})
tasks.getByName("clean").dependsOn(itClean)

tasks.create("it-prepare") {
  val prop = Properties()
  val propFile = File("${projectDir}/test/gradle.properties")
  prop.load(FileInputStream(propFile))
  prop["version"] = version
  prop.store(FileOutputStream(propFile), "Do not edit version. Will be overwritten by integration-tests")
}

tasks.create("it", GradleBuild::class.java, {
  setBuildFile("test/build.gradle.kts")
  getStartParameter().projectProperties["version"] = version as String
  setTasks(listOf("yggdrasil", "test"))
  dependsOn("install", "it-prepare")
})
tasks.getByName("test").finalizedBy("it")
