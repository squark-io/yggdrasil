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
              asNode().apply {
                appendNode("prerequisites").appendNode("maven", "3.5.0")
                appendNode("build").apply {
                  appendNode("resources").appendNode("resource").appendNode("directory",
                    "${resourcesDir.relativeTo(buildDir)}")
                  appendNode("plugins").appendNode("plugin").apply {
                    this.appendNode("groupId", "org.apache.maven.plugins")
                    this.appendNode("artifactId", "maven-plugin-plugin")
                    this.appendNode("version",
                      dependencyManagement.managedVersions["org.apache.maven.plugins:maven-plugin-plugin"])
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
    inputs.files(java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.buildDependencies,
      java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output.classesDirs)
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
  "jar" {
    enabled = false
    dependsOn(copyLib)
  }
  val publishToMavenLocal = "publishToMavenLocal" {
    outputs.dirs("$projectDir/test/build/test-results")
    dependsOn(":yggdrasil-core:publishToMavenLocal", ":yggdrasil-bootstrap:publishToMavenLocal")
  }
  val it by creating(GradleBuild::class) {
    inputs.files(copyLib.outputs.files, "$projectDir/test/src", "$projectDir/test/pom.xml")
    outputs.dirs("$projectDir/test/build/test-results")
    setBuildFile("test/build.gradle.kts")
    tasks = listOf("clean", "execMaven", "test")
    dependsOn(publishToMavenLocal, copyLib)
  }
  "test"(Test::class) {
    isScanForTestClasses = false
    dependsOn(it)
  }
}

dependencies {
  compile(kotlin("stdlib"))
  compile("commons-io:commons-io")
  compile("org.apache.maven:maven-core")
  compile("org.apache.maven:maven-plugin-api")
  compile("org.apache.maven.plugin-tools:maven-plugin-annotations")
  runtime("org.apache.maven.plugins:maven-plugin-plugin")
  compile(project(":yggdrasil-core"))
  compile(project(":yggdrasil-bootstrap"))
}
