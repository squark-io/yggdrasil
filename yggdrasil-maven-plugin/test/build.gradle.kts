import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.gradleScriptKotlin
import org.gradle.kotlin.dsl.kotlinModule
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
    classpath("com.wiredforcode:gradle-spawn-plugin:${dependencyVersions["gradle-spawn-plugin"]}")
    classpath("org.junit.platform:junit-platform-gradle-plugin:${dependencyVersions["junit-platform-gradle-plugin"]}")
  }
}

val junitVersion: String = dependencyVersions["junit-platform-gradle-plugin"]!!

plugins {
  kotlin("jvm")
}

apply {
  plugin("org.junit.platform.gradle.plugin")
}

repositories {
  mavenLocal()
  jcenter()
}

tasks {
  "clean" {
    doFirst {
      delete("$projectDir/target")
    }
  }
  "compileKotlin" { enabled = false }
  val depsAsStringList = dependencyVersions.entries.map { "-D${it.key}=${it.value}" }.toMutableList()
  depsAsStringList.add("-Dkotlin=$embeddedKotlinVersion")

  val updateVersion by creating(Exec::class) {
    val args = mutableListOf("mvn", "-B", "-U", "-e", "versions:set", "-DnewVersion=$version", "versions:commit")
    args.addAll(depsAsStringList)
    commandLine(args)
    dependsOn("assemble")
  }
  val execMaven by creating(Exec::class) {
    val args = mutableListOf("mvn", "-B", "-U", "-e", "clean", "package")
    args.addAll(depsAsStringList)
    commandLine(args)
    dependsOn(updateVersion)
  }
  val deleteLogFile by creating {
    File("$projectDir/build/test-results/main.log").takeIf { it.exists() }?.delete()
  }
  val port = Random().nextInt(2000) + 10000
  val startJvm by creating(SpawnProcessTask::class) {
    dependsOn(execMaven, deleteLogFile)
    command = "java -Dio.squark.yggdrasil.port=$port -jar $projectDir/target/yggdrasil-maven-plugin-test-$version-yggdrasil.jar $projectDir/build/test-results/main.log"
    ready = "Yggdrasil initiated"
  }
  afterEvaluate({
    "junitPlatformTest"(JavaExec::class) {
      systemProperties.put("io.squark.yggdrasil.port", "$port")
      dependsOn(startJvm)
      val killTask by creating(KillProcessTask::class)
      finalizedBy(killTask)
    }
  })
  "jar" { enabled = false }
}

dependencies {
  testCompile(kotlin("stdlib"))
  testCompile("org.junit.jupiter", "junit-jupiter-api", dependencyVersions["junit-jupiter"])
  testCompile("io.rest-assured", "rest-assured", dependencyVersions["rest-assured"])
  testRuntime("org.junit.jupiter", "junit-jupiter-engine", dependencyVersions["junit-jupiter"])
  testCompile("commons-io", "commons-io", dependencyVersions["commons-io"])
}
