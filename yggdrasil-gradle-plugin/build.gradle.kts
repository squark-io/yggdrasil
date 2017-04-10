
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

tasks.create("it", GradleBuild::class.java, {
  setBuildFile("test/build.gradle.kts")
  setTasks(listOf("yggdrasil", "test"))
  startParameter.showStacktrace = project.gradle.startParameter.showStacktrace
  dependsOn("install")
})
