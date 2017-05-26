import org.gradle.api.plugins.MavenPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild
import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.compileOnly
import org.gradle.script.lang.kotlin.configure
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.java
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.project
import org.gradle.script.lang.kotlin.repositories
import java.io.File
import java.io.FileInputStream
import java.util.Properties

val dependencyVersions: Map<String, String> by extra

buildscript {
  val dependencyVersions: Map<String, String> by extra
  repositories {
    gradleScriptKotlin()
    mavenLocal()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin", dependencyVersions["kotlin"]))
  }
}

apply {
  plugin("kotlin")
  plugin("maven")
  plugin("java")
}

repositories {
  mavenLocal()
  gradleScriptKotlin()
}

tasks {
  val itClean by creating(GradleBuild::class) {
    setBuildFile("test/build.gradle.kts")
    tasks = listOf("clean")
  }
  "clean" { dependsOn(itClean) }

  val coreConfig = configurations.maybeCreate("yggdrasil-core")
  val bootstrapConfig = configurations.maybeCreate("yggdrasil-bootstrap")
  java.sourceSets.create("yggdrasil-core")
  java.sourceSets.create("yggdrasil-bootstrap")
  dependencies.add("yggdrasil-core", project(":yggdrasil-core"))
  dependencies.add("yggdrasil-bootstrap", project(":yggdrasil-bootstrap"))
  val resourcesDir = java.sourceSets.findByName("main").output.resourcesDir
  val classesDir = File(buildDir, "target/classes")
  classesDir.mkdirs()
  java {
    sourceSets {
      "main" {
        java.outputDir = classesDir
      }
    }
  }
  val copyCore by creating(Copy::class) {
    from(project.files(coreConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-core"))
  }
  val copyBootstrap by creating(Copy::class) {
    from(project.files(bootstrapConfig))
    into(File(resourcesDir, "META-INF/yggdrasil-bootstrap"))
  }

  val pomTask by creating {
    doLast {
      configure<MavenPluginConvention> {
        pom().apply {
          packaging = "maven-plugin"
          this.withXml {
            asNode().appendNode("build").apply {
              appendNode("resources").appendNode("resource").appendNode("directory", "$resourcesDir")
              appendNode("plugins").appendNode("plugin").apply {
                this.appendNode("groupId", "org.apache.maven.plugins")
                this.appendNode("artifactId", "maven-plugin-plugin")
                this.appendNode("version", dependencyVersions["maven-plugin-plugin"])
                this.appendNode("configuration").apply {
                  this.appendNode("goalPrefix", "yggdrasil")
                  this.appendNode("skipErrorNoDescriptorsFound", "true")
                }
                this.appendNode("executions").apply {
                  this.appendNode("execution").apply {
                    this.appendNode("id", "descriptor")
                    this.appendNode("goals").appendNode("goal", "descriptor")
                  }
                  this.appendNode("execution").apply {
                    this.appendNode("id", "help")
                    this.appendNode("goals").appendNode("goal", "helpmojo")
                  }
                }
              }
            }
          }
        }.writeTo("$buildDir/pom.xml")
      }
    }
  }
  val execMaven by creating(Exec::class) {
    workingDir("$buildDir")
    commandLine("mvn", "-B", "-U", "-e", "package")
    dependsOn(copyCore, copyBootstrap, pomTask, "assemble")
  }
  val copyLib by creating(Copy::class) {
    from(File(buildDir, "target")) {
      include("*.jar")
    }
    from(buildDir) {
      include("*.pom")
    }
    into(File(buildDir, "libs"))
    dependsOn(execMaven)
  }
  "install" { dependsOn(copyLib) }
  val itPrepare by creating {
    val prop = Properties()
    val propFile = File("$projectDir/test/gradle.properties")
    prop.load(FileInputStream(propFile))
    prop["version"] = version
    propFile.writeText(prop.entries.joinToString(separator = "\n", transform = { "${it.key}=${it.value}" }))
  }
  val it by creating(GradleBuild::class) {
    setBuildFile("test/build.gradle.kts")
    startParameter.projectProperties["version"] = version as String
    tasks = listOf("execMaven", "test")
    dependsOn("install", itPrepare)
  }
  "test" { finalizedBy(it) }
  "jar" { enabled = false }
}

dependencies {
  compile(gradleApi())
  compile(kotlinModule("stdlib", dependencyVersions["kotlin"]))
  compile("commons-io:commons-io:${dependencyVersions["commons-io"]}")

  compile("org.apache.maven:maven-core:${dependencyVersions["maven"]}")
  compile("org.apache.maven:maven-plugin-api:${dependencyVersions["maven"]}")
  compile("org.apache.maven.plugin-tools:maven-plugin-annotations:${dependencyVersions["maven-plugin-annotations"]}")

  compileOnly(project(":yggdrasil-core"))
  compileOnly(project(":yggdrasil-bootstrap"))
}
