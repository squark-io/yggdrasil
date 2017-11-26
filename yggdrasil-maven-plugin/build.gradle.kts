import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
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
}

description = "Yggdrasil Maven Plugin"

plugins {
  kotlin("jvm")
}

repositories {
  mavenLocal()
}

tasks {
  val resourcesDir = java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.resourcesDir
  val classesDir = File(buildDir, "target/classes")
  classesDir.mkdirs()

  val copyClasses by creating(Copy::class) {
    from(java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.classesDirs)
    into(classesDir)
  }
  val generatePom by creating {
    publishing {
      publications {
        (findByName("MavenPublication") as DefaultMavenPublication).apply {
          pom {
            packaging = "maven-plugin"
            withXml {
              asNode().apply{
                appendNode("prerequisites").appendNode("maven", "3.5.0")
                appendNode("build").apply {
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
    inputs.files(java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.buildDependencies, java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.classesDirs)
    outputs.files("$buildDir/pom.xml")
    dependsOn("generatePomFileForMavenPublicationPublication")
  }
  val mavenPackage by creating(Exec::class) {
    workingDir("$buildDir")
    commandLine("mvn", "-B", "-U", "-e", "package")
    inputs.files(java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.classesDirs, "$buildDir/pom.xml")
    outputs.files("$buildDir/target/${project.name}-${project.version}.jar")
    dependsOn(copyClasses, generatePom)
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
      "$projectDir/test/src", "$projectDir/test/pom.xml")
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
  compile(kotlin("stdlib", dependencyVersions["kotlin"]))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")
  compile("org.apache.maven:maven-core:${dependencyVersions["maven"]}")
  compile("org.apache.maven:maven-plugin-api:${dependencyVersions["maven"]}")
  compile("org.apache.maven.plugin-tools:maven-plugin-annotations:${dependencyVersions["maven-plugin-annotations"]}")

  compile(project(":yggdrasil-core"))
  compile(project(":yggdrasil-bootstrap"))
}
