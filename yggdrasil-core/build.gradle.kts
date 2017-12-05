import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

description = "Yggdrasil core"

plugins {
  kotlin("jvm")
}

dependencies {
  compile(kotlin("stdlib"))
  compile("org.jboss.weld.se:weld-se-core")
  compile("org.jboss.weld.servlet:weld-servlet-core")
  compile("javax.enterprise:cdi-api")
  compile("javax.servlet:javax.servlet-api")
  compile("io.undertow:undertow-servlet")
  compile("io.undertow:undertow-core")
  compile("org.jboss.xnio:xnio-nio")
  compile("javax.ws.rs:javax.ws.rs-api")
  compile("org.jboss.resteasy:resteasy-jaxrs")
  compile("org.jboss.resteasy:resteasy-cdi")
  compile("org.jboss.resteasy:resteasy-jackson2-provider")
  compile("org.jboss.resteasy:resteasy-servlet-initializer")
  compile("javax.json:javax.json-api")
  compile("org.glassfish:javax.json")
  compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr353")
  compile("org.apache.logging.log4j:log4j-core")
  compile("org.apache.logging.log4j:log4j-web")
  provided(project(":yggdrasil-bootstrap"))
}
