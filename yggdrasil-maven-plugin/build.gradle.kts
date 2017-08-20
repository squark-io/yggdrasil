import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.repositories
import java.io.File
import java.io.FileInputStream
import java.util.Properties

val dependencyVersions: Map<String, String> by extra

buildscript {
  repositories {
    mavenLocal()
  }
  dependencies {
    classpath(kotlin("gradle-plugin"))
  }
}

description = "Yggdrasil Maven Plugin"

plugins {
  kotlin("jvm")
}

repositories {
  mavenLocal()
}

tasks {

  val coreConfig = configurations.maybeCreate("yggdrasil-core")
  val bootstrapConfig = configurations.maybeCreate("yggdrasil-bootstrap")
  java.sourceSets.create("yggdrasil-core")
  java.sourceSets.create("yggdrasil-bootstrap")
  dependencies.add("yggdrasil-core", project(":yggdrasil-core"))
  dependencies.add("yggdrasil-bootstrap", project(":yggdrasil-bootstrap"))
  val resourcesDir = java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.resourcesDir
  val classesDir = File(buildDir, "target/classes")
  classesDir.mkdirs()
  java {
    sourceSets {
      "main" {
        java.outputDir = classesDir
      }
    }
  }
  val copyCore by creating(Copy::class) {
    from(project.files(coreConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-core"))
  }
  val copyBootstrap by creating(Copy::class) {
    from(project.files(bootstrapConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-bootstrap"))
  }
  val generatePom by creating {
    inputs.files()
    configure<PublishingExtension> {
      publications {
        (findByName("MavenPublication") as DefaultMavenPublication).apply {
          pom {
            packaging = "maven-plugin"
            withXml {
              asNode().appendNode("build").apply {
                appendNode("resources").appendNode("resource").appendNode("directory",
                  "${resourcesDir.relativeTo(buildDir)}")
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
          }
          doLast {
            val pomFile = asNormalisedPublication().pomFile
            copy {
              from(pomFile)
              into(buildDir)
              rename { "pom.xml" }
            }
          }
        }
      }
    }
    inputs.files(copyCore.outputs.files, copyBootstrap.outputs.files)
    outputs.files("$buildDir/pom.xml")
    outputs.upToDateWhen { File("$buildDir/pom.xml").exists() }
    dependsOn("generatePomFileForMavenPublicationPublication")
  }
  val mavenPackage by creating(Exec::class) {
    workingDir("$buildDir")
    commandLine("mvn", "-B", "-U", "-e", "package")
    inputs.files(copyCore.outputs.files, copyBootstrap.outputs.files)
    outputs.files("$buildDir/target/${project.name}-${project.version}.jar")
    outputs.upToDateWhen { File("$buildDir/target/${project.name}-${project.version}.jar").exists() }
    dependsOn(copyCore, copyBootstrap, generatePom)
  }
  val copyLib by creating(Copy::class) {
    from(File(buildDir, "target")) {
      include("${project.name}-${project.version}.jar")
    }
    from(buildDir) {
      include("pom.xml")
    }
    into(File(buildDir, "libs"))
    dependsOn(mavenPackage)
  }
  val itPrepare by creating {
    val propFile = File("$projectDir/test/gradle.properties")
    inputs.files("$buildDir/libs/${project.name}-${project.version}.jar", "$buildDir/libs/pom.xml")
    outputs.file(propFile)
    doFirst {
      val prop = Properties()
      prop.load(FileInputStream(propFile))
      prop["version"] = version
      propFile.writeText(prop.entries.joinToString(separator = "\n", transform = { "${it.key}=${it.value}" }))
    }
  }
  val it by creating(GradleBuild::class) {
    inputs.files("$buildDir/libs/${project.name}-${project.version}.jar", "$buildDir/libs/pom.xml",
      "$projectDir/test/src")
    outputs.files("$projectDir/test/build/test-results")
    setBuildFile("test/build.gradle.kts")
    startParameter.projectProperties["version"] = version as String
    tasks = listOf("clean", "execMaven", "test")
    dependsOn("publishToMavenLocal", itPrepare, ":yggdrasil-core:publishToMavenLocal",
      ":yggdrasil-bootstrap:publishToMavenLocal")
  }
  "test"(Test::class) {
    isScanForTestClasses = false
    dependsOn(it)
  }
  "jar" {
    enabled = false
    dependsOn(copyLib)
  }
}

dependencies {
  compile(gradleApi())
  compile(kotlin("stdlib"))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")
  compile("org.apache.maven:maven-core:${dependencyVersions["maven"]}")
  compile("org.apache.maven:maven-plugin-api:${dependencyVersions["maven"]}")
  compile("org.apache.maven.plugin-tools:maven-plugin-annotations:${dependencyVersions["maven-plugin-annotations"]}")

  provided(project(":yggdrasil-core"))
  provided(project(":yggdrasil-bootstrap"))
}
