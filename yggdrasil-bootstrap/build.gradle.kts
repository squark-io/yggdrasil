import org.gradle.kotlin.dsl.dependencies

val dependencyVersions: Map<String, String> by extra

description = "Bootstrapper for Yggdrasil projects"

plugins {
  kotlin("jvm")
}

dependencies {
  compile(kotlin("stdlib", dependencyVersions["kotlin"]))
}
