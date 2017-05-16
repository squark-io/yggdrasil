
import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.repositories
import org.gradle.script.lang.kotlin.testCompile
import org.gradle.script.lang.kotlin.testRuntime
import java.io.File

buildscript {
  repositories {
    mavenLocal()
    gradleScriptKotlin()
    maven { setUrl("http://dl.bintray.com/vermeulen-mp/gradle-plugins") }
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin"))
    classpath("io.squark.yggdrasil:yggdrasil-gradle-plugin:${version}")
    classpath("com.wiredforcode:gradle-spawn-plugin:0.6.0")
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0-M4")
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

val deleteLogFile = tasks.create("deleteLogFile") {
  File("$projectDir/build/test-results/main.log").takeIf { it.exists() }?.delete()
}

val startJvm = tasks.create("startJvm", SpawnProcessTask::class.java, {
  dependsOn("yggdrasil", deleteLogFile)
  command = "java -jar $projectDir/build/libs/yggdrasil-gradle-plugin-test-${version}-yggdrasil.jar $projectDir/build/test-results/main.log"
  ready = "Yggdrasil initiated"

})
afterEvaluate({
  tasks.getByName("junitPlatformTest").dependsOn(startJvm)
    .finalizedBy(tasks.create("stopJvm", KillProcessTask::class.java))
})

dependencies {
  compile(kotlinModule("stdlib"))
  compileOnly("javax.enterprise", "cdi-api", "2.0")
  compileOnly("javax.ws.rs", "javax.ws.rs-api", "2.0.1")
  compileOnly("javax.json", "javax.json-api", "1.1.0-M1")
  compile("org.apache.logging.log4j", "log4j-api", "2.8.1")
  compileOnly("org.apache.logging.log4j", "log4j-core", "2.8.1")
  testCompile("org.junit.jupiter:junit-jupiter-api:5.0.0-M4")
  testCompile("io.rest-assured:rest-assured:3.0.2")
  testCompile("commons-io:commons-io:2.5")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.0-M4")
}
