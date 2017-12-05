import org.gradle.kotlin.dsl.dependencies

description = "Bootstrapper for Yggdrasil projects"

plugins {
  kotlin("jvm")
}

dependencies {
  compile(kotlin("stdlib"))
}
