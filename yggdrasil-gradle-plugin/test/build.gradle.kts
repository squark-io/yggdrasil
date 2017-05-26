import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.api.tasks.JavaExec
import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.repositories
import org.gradle.script.lang.kotlin.testCompile
import org.gradle.script.lang.kotlin.testRuntime
import java.io.File
import java.util.Random

val dependencyVersions: Map<String, String> by extra

buildscript {
  applyFrom("../../versions.gradle.kts")
  val dependencyVersions: Map<String, String> by extra
  repositories {
    mavenLocal()
    gradleScriptKotlin()
    maven { setUrl("http://dl.bintray.com/vermeulen-mp/gradle-plugins") }
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin", dependencyVersions["kotlin"]))
    classpath("io.squark.yggdrasil:yggdrasil-gradle-plugin:${version}")
    classpath("com.wiredforcode:gradle-spawn-plugin:${dependencyVersions["gradle-spawn-plugin"]}")
    classpath("org.junit.platform:junit-platform-gradle-plugin:${dependencyVersions["junit-platform-gradle-plugin"]}")
  }
}

apply {
  plugin("kotlin")
  plugin("io.squark.yggdrasil.yggdrasil-gradle-plugin")
  plugin("org.junit.platform.gradle.plugin")
}

repositories {
  gradleScriptKotlin()
  mavenLocal()
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
    "junitPlatformTest"(JavaExec::class) {
      systemProperties.put("io.squark.yggdrasil.port", "$port")
      dependsOn(startJvm)
      val killTask by creating(KillProcessTask::class)
      finalizedBy(killTask)
    }
  }
}

dependencies {
  compile(kotlinModule("stdlib", dependencyVersions["kotlin"]))
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
