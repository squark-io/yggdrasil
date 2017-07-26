import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlinModule
import org.gradle.kotlin.dsl.project

val dependencyVersions: Map<String, String> by extra

buildscript {
  dependencies {
    classpath(kotlinModule("gradle-plugin"))
  }
}

plugins {
  kotlin("jvm")
  maven
}

dependencies {
  compile(kotlinModule("stdlib", dependencyVersions["kotlin"]))
  compile("org.jboss.weld.se", "weld-se-core", dependencyVersions["weld"])
  compile("org.jboss.weld.servlet", "weld-servlet-core", dependencyVersions["weld"])
  compile("javax.enterprise", "cdi-api", dependencyVersions["cdi-api"])
  compile("javax.servlet", "javax.servlet-api", dependencyVersions["servlet-api"])
  compile("io.undertow", "undertow-servlet", dependencyVersions["undertow"])
  compile("io.undertow", "undertow-core", dependencyVersions["undertow"])
  compile("javax.ws.rs", "javax.ws.rs-api", dependencyVersions["rs-api"])
  compile("org.jboss.resteasy", "resteasy-jaxrs", dependencyVersions["resteasy"])
  compile("org.jboss.resteasy", "resteasy-cdi", dependencyVersions["resteasy"])
  compile("org.jboss.resteasy", "resteasy-jackson2-provider", dependencyVersions["resteasy"])
  compile("org.jboss.resteasy", "resteasy-servlet-initializer", dependencyVersions["resteasy"])
  compile("javax.json", "javax.json-api", dependencyVersions["javax.json"])
  compile("org.glassfish", "javax.json", dependencyVersions["javax.json"])
  compile("com.fasterxml.jackson.datatype", "jackson-datatype-jsr353", dependencyVersions["jackson-datatype-jsr353"])
  compile("org.apache.logging.log4j", "log4j-core", dependencyVersions["log4j"])
  compile("org.apache.logging.log4j", "log4j-web", dependencyVersions["log4j"])
  compileOnly(project(":yggdrasil-bootstrap"))
}
