import com.jfrog.bintray.gradle.BintrayExtension
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.plugins.JavaPlugin

import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.archives
import org.gradle.kotlin.dsl.base
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.dokka.gradle.LinkMapping

val dependencyVersions: Map<String, String> by extra

allprojects {
  group = "io.squark.yggdrasil"
  version = "0.2.2"

  repositories {
    jcenter()
    mavenLocal()
  }

  buildscript {
    repositories {
      jcenter()
    }
    dependencies {
      classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${dependencyVersions["gradle-bintray-plugin"]}")
      classpath("org.jetbrains.dokka:dokka-gradle-plugin:${dependencyVersions["dokka-gradle-plugin"]}")
    }
  }
}

buildscript {
  applyFrom("versions.gradle.kts")
  val dependencyVersions: Map<String, String> by extra

  repositories {
    jcenter()
  }
  dependencies {
    classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${dependencyVersions["gradle-bintray-plugin"]}")
    classpath("org.jetbrains.dokka:dokka-gradle-plugin:${dependencyVersions["dokka-gradle-plugin"]}")
  }
}

apply {
  plugin("com.jfrog.bintray")
  plugin("java")
}

plugins {
  base
  java
}

dependencies {
  // Make the root project archives configuration depend on every subproject
  subprojects.forEach {
    archives(it)
  }
}

configure(subprojects) {
  val subproject = this
  apply {
    plugin("maven-publish")
    plugin("com.jfrog.bintray")
    plugin("org.jetbrains.dokka")
  }

  configure<PublishingExtension> {
    publications {
      this.create("MavenPublication", MavenPublication::class.java, {
        from(components["java"])
        artifact(tasks.getByPath("sourceJar"), {
          classifier = "sources"
        })
        artifact(tasks.getByPath("javadocJar"), {
          classifier = "javadoc"
        })
        pom.withXml {
          val dependencies: Node = when ((asNode()["dependencies"] as NodeList).size) {
            0 -> {
              asNode().appendNode("dependencies")
            }
            else -> {
              (asNode()["dependencies"] as NodeList)[0] as Node
            }
          }
          configurations["provided"].allDependencies.forEach {
            val dependencyNode = dependencies.appendNode("dependency")
            dependencyNode.appendNode("groupId", it.group)
            dependencyNode.appendNode("artifactId", it.name)
            dependencyNode.appendNode("version", it.version)
            dependencyNode.appendNode("scope", "provided")
          }
          asNode().apply {
            appendNode("name", project.name)
            appendNode("description",
              subproject.description ?: throw GradleException("Project $subproject has not set required description"))
            appendNode("url", "http://yggdrasil.squark.io")
            appendNode("inceptionYear", "2016")
            appendNode("distributionManagement").apply {
              appendNode("repository").apply {
                appendNode("id", "bintray-squark-io-squark.io")
                appendNode("name", "squark-io-squark.io")
                appendNode("url", "https://api.bintray.com/maven/squark-io/squark.io/yggdrasil/")
              }
            }
            appendNode("licenses").apply {
              appendNode("license").apply {
                appendNode("name", "Apache License, Version 2.0")
                appendNode("url", "https://www.apache.org/licenses/LICENSE-2.0.txt")
                appendNode("distribution", "repo")
                appendNode("comments", "A business-friendly OSS license")
              }
            }
            appendNode("organization").apply {
              appendNode("name", "squark.io")
              appendNode("url", "https://squark.io")
            }
            appendNode("contributors").apply {
              appendNode("contributor").apply {
                appendNode("name", "Erik Håkansson")
                appendNode("email", "erik@squark.io")
              }
            }
            appendNode("developers").apply {
              appendNode("developer").apply {
                appendNode("name", "Erik Håkansson")
                appendNode("email", "erik@squark.io")
              }
            }
            appendNode("scm").apply {
              appendNode("url", "https://github.com/squark-io/yggdrasil")
              appendNode("connection", "scm:git:git@github.com:squark-io/yggdrasil.git")
              appendNode("tag", "HEAD")
            }
          }
        }
      })
    }
  }

  configure<BintrayExtension> {
    user = if (project.hasProperty("bintrayUser")) (project.property("bintrayUser") as String) else System.getenv(
      "BINTRAY_USER")
    key = if (project.hasProperty("bintrayApiKey")) (project.property("bintrayApiKey") as String) else System.getenv(
      "BINTRAY_API_KEY")
    dryRun = false
    pkg(closureOf<BintrayExtension.PackageConfig> {
      repo = "squark.io"
      name = "yggdrasil"
      userOrg = "squark-io"
      setLicenses("Apache-2.0")
      vcsUrl = "https://github.com/squark-io/yggdrasil/"
    })
    setPublications("MavenPublication")
  }

  plugins.withType(JavaPlugin::class.java).whenPluginAdded {
    configurations {
      this.create("provided", {
        extendsFrom(configurations.compileOnly)
      })
    }
    java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].compileClasspath += configurations["provided"]
    java.sourceSets[SourceSet.TEST_SOURCE_SET_NAME].compileClasspath += configurations["provided"]
    java.sourceSets[SourceSet.TEST_SOURCE_SET_NAME].runtimeClasspath += configurations["provided"]
    tasks {
      val sourceJar by creating(Jar::class) {
        classifier = "sources"
        dependsOn("classes")
        from(java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
      }
      val dokkaTask by creating(DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
        sourceDirs = files("src/main")
        val linkMapping = LinkMapping().apply {
          dir = "src/main/kotlin"
          url = "https://github.com/squark-io/yggdrasil"
          suffix = "#L"
        }
        linkMappings = arrayListOf(linkMapping)
      }
      val javadocJar by creating(Jar::class) {
        classifier = "javadoc"
        dependsOn("javadoc", dokkaTask)
        from((tasks.getByName("javadoc") as Javadoc).destinationDir, dokkaTask.outputDirectory)
      }
      findByName("bintrayUpload").dependsOn(subprojects.map { it.tasks.getByName("bintrayUpload") })
    }
  }
}
