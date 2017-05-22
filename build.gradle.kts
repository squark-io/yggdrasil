import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.plugins.JavaPlugin

import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.script.lang.kotlin.archives
import org.gradle.script.lang.kotlin.base
import org.gradle.script.lang.kotlin.closureOf
import org.gradle.script.lang.kotlin.configure
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.get
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.java
import org.gradle.script.lang.kotlin.repositories

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
        from(java.sourceSets["main"].allSource)
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
