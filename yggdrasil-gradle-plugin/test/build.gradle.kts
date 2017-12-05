import java.io.FileInputStream
import java.util.Properties
import java.util.regex.Pattern

import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.compile
import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.testCompile
import org.gradle.kotlin.dsl.testRuntime
import java.io.File
import java.util.Random

task<Wrapper>("wrapper") {
  val wrapperProps = Properties()
  wrapperProps.load(FileInputStream(File("$projectDir/../../gradle/wrapper/gradle-wrapper.properties")))
  val distributionUrl = (wrapperProps["distributionUrl"] as String)
  val pattern = Pattern.compile(".*gradle-(.*)-all.zip")
  val matcher = pattern.matcher(distributionUrl)
  matcher.find()
  gradleVersion = matcher.group(1)
  distributionType = Wrapper.DistributionType.ALL
  jarFile = File("$projectDir/../../gradle/wrapper/gradle-wrapper.jar")
}

buildscript {
  project.apply {
    from("../../version.gradle.kts")
  }
  repositories {
    jcenter()
    mavenLocal()
    maven { setUrl("http://dl.bintray.com/vermeulen-mp/gradle-plugins") }
  }
  dependencies {
    classpath("io.squark.yggdrasil:yggdrasil-gradle-plugin:$version")
    classpath("io.spring.gradle:dependency-management-plugin:1.0.3.RELEASE")
    classpath("com.wiredforcode:gradle-spawn-plugin:0.6.0")
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
  }
}

plugins {
  kotlin("jvm") version "1.1.51"
}

apply {
  plugin("io.squark.yggdrasil.yggdrasil-gradle-plugin")
  plugin("org.junit.platform.gradle.plugin")
  plugin("io.spring.dependency-management")
}

repositories {
  mavenLocal()
  jcenter()
}

dependencyManagement {
  dependencies {
    imports {
      mavenBom("io.squark.yggdrasil:yggdrasil-gradle-plugin:$version")
    }
  }
}

tasks {
  val deleteLogFile by creating {
    File("$projectDir/build/test-results/main.log").takeIf { it.exists() }?.delete()
  }
  val port = Random().nextInt(2000) + 10000
  val startJvm by creating(SpawnProcessTask::class) {
    dependsOn("yggdrasil", deleteLogFile)
    command = "java -Dio.squark.yggdrasil.port=$port -jar $projectDir/build/libs/yggdrasil-gradle-plugin-test-$version-yggdrasil.jar $projectDir/build/test-results/main.log"
    ready = "Yggdrasil initiated"
  }
  afterEvaluate {
    val killTask by creating(KillProcessTask::class)
    "junitPlatformTest"(JavaExec::class) {
      systemProperties.put("io.squark.yggdrasil.port", "$port")
      systemProperties.put("project-version", "$version")
      dependsOn(startJvm)
      finalizedBy(killTask)
    }
  }
}

dependencies {
  compile(kotlin("stdlib"))
  compileOnly("javax.enterprise:cdi-api")
  compileOnly("javax.ws.rs:javax.ws.rs-api")
  compileOnly("javax.json:javax.json-api")
  compileOnly("org.apache.logging.log4j:log4j-core")
  compile("com.google.guava:guava:23.2-jre")
  testCompile("commons-io:commons-io")
  testCompile("io.rest-assured:rest-assured")
  testCompile("org.junit.jupiter:junit-jupiter-api")
  testRuntime("org.junit.jupiter:junit-jupiter-engine")
}
