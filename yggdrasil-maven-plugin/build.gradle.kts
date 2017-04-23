import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.MavenPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild
import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.configure
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.java
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.project
import org.gradle.script.lang.kotlin.repositories
import org.gradle.script.lang.kotlin.task
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

buildscript {
  repositories {
    gradleScriptKotlin()
    mavenLocal()
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

repositories {
  mavenLocal()
  gradleScriptKotlin()
}

configurations.maybeCreate("yggdrasil-core")
configurations.maybeCreate("yggdrasil-bootstrap")
java.sourceSets.create("yggdrasil-core")
java.sourceSets.create("yggdrasil-bootstrap")
dependencies.add("yggdrasil-core", project(":yggdrasil-core"))
dependencies.add("yggdrasil-bootstrap", project(":yggdrasil-bootstrap"))

val resourcesDir = java.sourceSets.findByName("main").output.resourcesDir
val classesDir = File(buildDir, "target/classes")
classesDir.mkdirs()
java.sourceSets.findByName("main").output.setClassesDir(classesDir)
val copyCore = tasks.create("copyCore", Copy::class.java, {
  from(project.files(project.configurations.getByName("yggdrasil-core")))
  into(File(resourcesDir, "META-INF/yggdrasil-core"))
})
val copyBootstrap = tasks.create("copyBootstrap", Copy::class.java, {
  from(project.files(project.configurations.getByName("yggdrasil-bootstrap")))
  into(File(resourcesDir, "META-INF/yggdrasil-bootstrap"))
})

tasks.findByName("jar").enabled = false

val pomTask = task("pomTask") {
  doLast {
    configure<MavenPluginConvention> {
      pom().apply {
        packaging = "maven-plugin"

        this.withXml {
          asNode().appendNode("build").apply {
            appendNode("resources").appendNode("resource").appendNode("directory", "${resourcesDir}")
            appendNode("plugins").appendNode("plugin").apply {
              this.appendNode("groupId", "org.apache.maven.plugins")
              this.appendNode("artifactId", "maven-plugin-plugin")
              this.appendNode("version", "3.4")
              this.appendNode("configuration").apply {
                this.appendNode("goalPrefix", "yggdrasil")
                this.appendNode("skipErrorNoDescriptorsFound", "true")
              }
              this.appendNode("executions").apply {
                this.appendNode("execution").apply {
                  this.appendNode("id", "descriptor")
                  this.appendNode("goals").appendNode("goal", "descriptor")
                }
                this.appendNode("execution").apply {
                  this.appendNode("id", "help")
                  this.appendNode("goals").appendNode("goal", "helpmojo")
                }
              }
            }
          }
        }
      }.writeTo("$buildDir/pom.xml")
    }
  }
}

val execMaven = task<Exec>("execMaven") {
  dependsOn(copyCore, copyBootstrap)
  workingDir("$buildDir")
  commandLine("mvn", "-B", "-U", "-e", "package")
  dependsOn(pomTask, "assemble")
}

val copyLib = task<Copy>("copyMavenJar") {
  from(File(buildDir, "target")) {
    include("*.jar")
  }
  from(buildDir) {
    include("*.pom")
  }
  into(File(buildDir, "libs"))
  dependsOn(execMaven)
}

tasks.findByName("install").dependsOn(copyLib)

val mavenVersion = "3.5.0"

dependencies {
  compile(gradleApi())
  compile(kotlinModule("stdlib"))
  compile("commons-io:commons-io:2.5")

  compile("org.apache.maven:maven-core:$mavenVersion")
  compile("org.apache.maven:maven-plugin-api:$mavenVersion")
  compile("org.apache.maven.plugin-tools:maven-plugin-annotations:3.4")

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
  val propFile = File("$projectDir/test/gradle.properties")
  prop.load(FileInputStream(propFile))
  prop["version"] = version
  prop.store(FileOutputStream(propFile), "Do not edit version. Will be overwritten by integration-tests")
}

tasks.create("it", GradleBuild::class.java, {
  setBuildFile("test/build.gradle.kts")
  startParameter.projectProperties["version"] = version as String
  setTasks(listOf("execMaven", "test"))
  dependsOn("install", "it-prepare")
})
tasks.getByName("test").finalizedBy("it")

