
import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.api.tasks.Exec
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.repositories
import org.gradle.script.lang.kotlin.task
import org.gradle.script.lang.kotlin.testCompile
import org.gradle.script.lang.kotlin.testRuntime

buildscript {
  repositories {
    mavenLocal()
    gradleScriptKotlin()
    maven { setUrl("http://dl.bintray.com/vermeulen-mp/gradle-plugins") }
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin"))
    classpath("com.wiredforcode:gradle-spawn-plugin:0.6.0")
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0-M4")
  }
}

apply {
  plugin("kotlin")
  plugin("org.junit.platform.gradle.plugin")
}

repositories {
  gradleScriptKotlin()
  mavenLocal()
}

tasks.findByName("clean").doFirst({
  delete("$projectDir/target")
})

tasks.findByName("compileKotlin").enabled = false

val updateVersion = task<Exec>("updateVersion") {
  commandLine("mvn", "-B", "-U", "-e", "versions:set", "-DnewVersion=$version", "versions:commit")
  dependsOn("assemble")
}

val execMaven = task<Exec>("execMaven") {
  commandLine("mvn", "-B", "-U", "-e", "clean", "package")
  dependsOn(updateVersion)
}

val startJvm = tasks.create("startJvm", SpawnProcessTask::class.java, {
  dependsOn(execMaven)
  command = "java -jar $projectDir/target/yggdrasil-maven-plugin-test-$version-yggdrasil.jar"
  ready = "Yggdrasil initiated"
})
afterEvaluate({
  tasks.getByName("junitPlatformTest").dependsOn(startJvm)
    .finalizedBy(tasks.create("stopJvm", KillProcessTask::class.java))
})

dependencies {
  testCompile(kotlinModule("stdlib"))
  testCompile("org.junit.jupiter:junit-jupiter-api:5.0.0-M4")
  testCompile("io.rest-assured:rest-assured:3.0.2")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.0.0-M4")
}
