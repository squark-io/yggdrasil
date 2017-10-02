import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.gradleScriptKotlin
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.testCompile
import org.gradle.kotlin.dsl.testRuntime
import java.io.File
import java.util.Random

val dependencyVersions: Map<String, String> by extra

buildscript {
  project.apply {
    from("../../versions.gradle.kts")
  }
  val dependencyVersions: Map<String, String> by extra
  repositories {
    mavenLocal()
    maven { setUrl("http://dl.bintray.com/vermeulen-mp/gradle-plugins") }
  }
  dependencies {
    classpath(kotlin("gradle-plugin"))
    classpath("io.squark.yggdrasil:yggdrasil-gradle-plugin:${version}")
    classpath("com.wiredforcode:gradle-spawn-plugin:${dependencyVersions["gradle-spawn-plugin"]}")
    classpath("org.junit.platform:junit-platform-gradle-plugin:${dependencyVersions["junit-platform-gradle-plugin"]}")
  }
}

plugins {
  kotlin("jvm")
}

apply {
  plugin("io.squark.yggdrasil.yggdrasil-gradle-plugin")
  plugin("org.junit.platform.gradle.plugin")
}

repositories {
  mavenLocal()
  jcenter()
}

tasks {
  val deleteLogFile by creating {
    File("$projectDir/build/test-results/main.log").takeIf { it.exists() }?.delete()
  }
  val port = Random().nextInt(2000) + 10000
  val startJvm by creating(SpawnProcessTask::class) {
    dependsOn("yggdrasil", deleteLogFile)
    command = "java -Dio.squark.yggdrasil.port=$port -jar $projectDir/build/libs/yggdrasil-gradle-plugin-test-${version}-yggdrasil.jar $projectDir/build/test-results/main.log"
    ready = "Yggdrasil initiated"
  }
  afterEvaluate {
    val killTask by creating(KillProcessTask::class)
    "junitPlatformTest"(JavaExec::class) {
      systemProperties.put("io.squark.yggdrasil.port", "$port")
      dependsOn(startJvm)
      finalizedBy(killTask)
    }
  }
}

dependencies {
  compile(kotlin("stdlib"))
  compileOnly("javax.enterprise", "cdi-api", dependencyVersions["cdi-api"])
  compileOnly("javax.ws.rs", "javax.ws.rs-api", dependencyVersions["rs-api"])
  compileOnly("javax.json", "javax.json-api", dependencyVersions["javax.json"])
  compile("org.apache.logging.log4j", "log4j-api", dependencyVersions["log4j"])
  compileOnly("org.apache.logging.log4j", "log4j-core", dependencyVersions["log4j"])
  testCompile("commons-io", "commons-io", dependencyVersions["commons-io"])
  testCompile("io.rest-assured", "rest-assured", dependencyVersions["rest-assured"])
  testCompile("org.junit.jupiter", "junit-jupiter-api", dependencyVersions["junit-jupiter"])
  testRuntime("org.junit.jupiter", "junit-jupiter-engine", dependencyVersions["junit-jupiter"])
}
