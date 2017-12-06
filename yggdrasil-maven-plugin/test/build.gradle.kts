import com.wiredforcode.gradle.spawn.KillProcessTask
import com.wiredforcode.gradle.spawn.SpawnProcessTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.testCompile
import org.gradle.kotlin.dsl.testRuntime
import java.io.File
import java.util.Properties
import java.io.FileInputStream
import java.util.Random
import java.util.regex.Pattern

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
    from("../../versions.gradle.kts")
  }
  repositories {
    jcenter()
    mavenLocal()
    maven { setUrl("http://dl.bintray.com/vermeulen-mp/gradle-plugins") }
  }
  val dependencyVersions: Map<String, String> by extra
  dependencies {
    classpath("io.spring.gradle:dependency-management-plugin:${dependencyVersions["dependency-management-plugin"]}")
    classpath("com.wiredforcode:gradle-spawn-plugin:${dependencyVersions["gradle-spawn-plugin"]}")
    classpath("org.junit.platform:junit-platform-gradle-plugin:${dependencyVersions["junit-platform-gradle-plugin"]}")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${dependencyVersions["kotlin"]}")
  }
}

apply {
  plugin("org.junit.platform.gradle.plugin")
  plugin("io.spring.dependency-management")
  plugin("org.jetbrains.kotlin.jvm")
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
  "clean" {
    doFirst {
      delete("$projectDir/target")
    }
  }
  "compileKotlin" { enabled = false }
  val depsAsStringList = dependencyManagement.managedVersions.entries.map {
    "-D${it.key.split(':')[1]}=${it.value}"
  }.toMutableList()

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
      systemProperties.put("project-version", "$version")
      dependsOn(startJvm)
      val killTask by creating(KillProcessTask::class)
      finalizedBy(killTask)
    }
  })
  "jar" { enabled = false }
}

dependencies {
  testCompile(kotlin("stdlib"))
  testCompile("org.junit.jupiter:junit-jupiter-api")
  testCompile("io.rest-assured:rest-assured")
  testRuntime("org.junit.jupiter:junit-jupiter-engine")
  testCompile("commons-io:commons-io")
}
