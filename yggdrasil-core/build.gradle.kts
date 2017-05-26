import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.project
import org.gradle.script.lang.kotlin.repositories

val dependencyVersions: Map<String, String> by extra

buildscript {
  val dependencyVersions: Map<String, String> by extra
  repositories {
    gradleScriptKotlin()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin", dependencyVersions["kotlin"]))
  }
}

apply {
  plugin("kotlin")
  plugin("maven")
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
