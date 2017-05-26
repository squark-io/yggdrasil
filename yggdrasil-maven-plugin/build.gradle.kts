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

val dependencyVersions: Map<String, String> by extra

buildscript {
  val dependencyVersions: Map<String, String> by extra
  repositories {
    gradleScriptKotlin()
    mavenLocal()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin", dependencyVersions["kotlin"]))
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

java {
  sourceSets {
    "main" {
      java.outputDir = classesDir
    }
  }
}
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
            appendNode("resources").appendNode("resource").appendNode("directory", "$resourcesDir")
            appendNode("plugins").appendNode("plugin").apply {
              this.appendNode("groupId", "org.apache.maven.plugins")
              this.appendNode("artifactId", "maven-plugin-plugin")
              this.appendNode("version", dependencyVersions["maven-plugin-plugin"])
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

val itClean = tasks.create("it-clean", GradleBuild::class.java, {
  setBuildFile("test/build.gradle.kts")
  tasks = listOf("clean")
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
  tasks = listOf("execMaven", "test")
  dependsOn("install", "it-prepare")
})
tasks.getByName("test").finalizedBy("it")

tasks.getByName("jar").enabled = false

dependencies {
  compile(gradleApi())
  compile(kotlinModule("stdlib", dependencyVersions["kotlin"]))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")

  compile("org.apache.maven:maven-core:${dependencyVersions["maven"]}")
  compile("org.apache.maven:maven-plugin-api:${dependencyVersions["maven"]}")
  compile("org.apache.maven.plugin-tools:maven-plugin-annotations:${dependencyVersions["maven-plugin-annotations"]}")

  compileOnly(project(":yggdrasil-core"))
  compileOnly(project(":yggdrasil-bootstrap"))
}
