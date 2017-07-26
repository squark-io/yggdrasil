import org.gradle.kotlin.dsl.dependencies

val dependencyVersions: Map<String, String> by extra
buildscript {
  dependencies {
    classpath(kotlin("gradle-plugin"))
  }
}

plugins {
  kotlin("jvm")
  maven
}

dependencies {
  compile(kotlin("stdlib", dependencyVersions["kotlin"]))
}
