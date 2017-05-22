import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.repositories
import org.gradle.script.lang.kotlin.task
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
    classpath(kotlinModule("gradle-plugin"))
    classpath("com.wiredforcode:gradle-spawn-plugin:${dependencyVersions["gradle-spawn-plugin"]}")
    classpath("org.junit.platform:junit-platform-gradle-plugin:${dependencyVersions["junit-platform-gradle-plugin"]}")
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
  val depsAsStringList = dependencyVersions.entries.map { "-D${it.key}=${it.value}" }
  val args = mutableListOf("mvn", "-B", "-U", "-e", "clean", "package")
  args.addAll(depsAsStringList)
  args.add("-Dkotlin.version=$embeddedKotlinVersion")
  commandLine(args)
  dependsOn(updateVersion)
}

val deleteLogFile = tasks.create("deleteLogFile") {
  File("$projectDir/build/test-results/main.log").takeIf { it.exists() }?.delete()
}

val port = Random().nextInt(2000) + 10000
val startJvm = tasks.create("startJvm", SpawnProcessTask::class.java, {
  dependsOn(execMaven, deleteLogFile)
  command = "java -Dio.squark.yggdrasil.port=$port -jar $projectDir/target/yggdrasil-maven-plugin-test-$version-yggdrasil.jar $projectDir/build/test-results/main.log"
  ready = "Yggdrasil initiated"
})
afterEvaluate({
  (tasks.getByName("junitPlatformTest") as JavaExec).apply {
    systemProperties.put("io.squark.yggdrasil.port", "$port")
    dependsOn(startJvm)
    finalizedBy(tasks.create("stopJvm", KillProcessTask::class.java))
  }
})

tasks.getByName("jar").enabled = false

dependencies {
  testCompile(kotlinModule("stdlib"))
  testCompile("org.junit.jupiter", "junit-jupiter-api", dependencyVersions["junit-jupiter"])
  testCompile("io.rest-assured", "rest-assured", dependencyVersions["rest-assured"])
  testRuntime("org.junit.jupiter", "junit-jupiter-engine", dependencyVersions["junit-jupiter"])
  testCompile("commons-io", "commons-io", dependencyVersions["commons-io"])
}
