import org.gradle.kotlin.dsl.dependencies

val dependencyVersions: Map<String, String> by extra
buildscript {
  dependencies {
    classpath(kotlin("gradle-plugin"))
  }
}

description = "Bootstrapper for Yggdrasil projects"

plugins {
  kotlin("jvm")
}

dependencies {
  compile(kotlin("stdlib", dependencyVersions["kotlin"]))
}
