import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.project
import org.gradle.script.lang.kotlin.repositories

buildscript {
  repositories {
    gradleScriptKotlin()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin"))
  }
}

apply {
  plugin("kotlin")
  plugin("maven")
}

dependencies {
  compile(kotlinModule("stdlib"))
  compile("org.jboss.weld.se", "weld-se-core", "3.0.0.CR2")
  compile("org.jboss.weld.servlet", "weld-servlet-core", "3.0.0.CR2")
  compile("javax.enterprise", "cdi-api", "2.0-PFD")
  compile("javax.servlet", "javax.servlet-api", "3.1.0")
  compile("io.undertow", "undertow-servlet", "1.4.11.Final")
  compile("io.undertow", "undertow-core", "1.4.11.Final")
  compile("javax.ws.rs", "javax.ws.rs-api", "2.0.1")
  compile("org.jboss.resteasy", "resteasy-jaxrs", "3.1.2.Final")
  compile("org.jboss.resteasy", "resteasy-cdi", "3.1.2.Final")
  compile("org.jboss.resteasy", "resteasy-jackson2-provider", "3.1.2.Final")
  compile("org.jboss.resteasy", "resteasy-servlet-initializer", "3.1.2.Final")
  compile("javax.json", "javax.json-api", "1.1.0-M1")
  compile("org.glassfish", "javax.json", "1.1.0-M1")
  compile("com.fasterxml.jackson.datatype", "jackson-datatype-jsr353", "2.8.7")
  compile("org.apache.logging.log4j", "log4j-core", "2.8.1")
  compile("org.apache.logging.log4j", "log4j-web", "2.8.1")
  compileOnly(project(":yggdrasil-bootstrap"))
}
