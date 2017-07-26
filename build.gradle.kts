import com.jfrog.bintray.gradle.BintrayExtension
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
import org.gradle.kotlin.dsl.gradleScriptKotlin
import org.gradle.kotlin.dsl.java
import org.gradle.kotlin.dsl.repositories

val dependencyVersions: Map<String, String> by extra

allprojects {
  group = "io.squark.yggdrasil"
  version = "0.2.0"

  repositories {
    gradleScriptKotlin()
    jcenter()
    mavenLocal()
    maven({
      setUrl("http://repository.jboss.org/nexus/content/groups/public")
    })
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

  apply {
    plugin("maven")
    plugin("com.jfrog.bintray")
    plugin("org.jetbrains.dokka")
  }

  configure<BintrayExtension> {
    user = if (project.hasProperty("bintrayUser")) (project.property("bintrayUser") as String) else System.getenv(
      "BINTRAY_USER")
    key = if (project.hasProperty("bintrayApiKey")) (project.property("bintrayApiKey") as String) else System.getenv(
      "BINTRAY_API_KEY")
    dryRun = false
    setConfigurations("archives")
    pkg(closureOf<BintrayExtension.PackageConfig> {
      repo = "squark.io"
      name = "yggdrasil"
      userOrg = "squark-io"
      setLicenses("Apache-2.0")
      vcsUrl = "https://github.com/squark-io/yggdrasil/"
    })
  }

  plugins.withType(JavaPlugin::class.java).whenPluginAdded {
    tasks {
      val sourcesJar by creating(Jar::class) {
        classifier = "sources"
        dependsOn("classes")
        from(java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
      }
      val dokkaTask by creating(DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
      }
      val javadocsJar by creating(Jar::class) {
        classifier = "javadoc"
        dependsOn("javadoc", dokkaTask)
        from((tasks.getByName("javadoc") as Javadoc).destinationDir, "$buildDir/javadoc")
      }
      artifacts {
        add("archives", sourcesJar)
        add("archives", javadocsJar)
      }
      findByName("bintrayUpload").dependsOn(subprojects.map { it.tasks.getByName("bintrayUpload") })
    }
  }
}
