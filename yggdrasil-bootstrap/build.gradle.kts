import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.kotlinModule
import org.gradle.script.lang.kotlin.repositories

val dependencyVersions: Map<String, String> by extra
buildscript {
  val dependencyVersions: Map<String, String> by extra
  repositories {
    gradleScriptKotlin()
  }
  dependencies {
    classpath(kotlinModule("gradle-plugin", dependencyVersions["kotlin"]))
  }
}

apply {
  plugin("kotlin")
  plugin("maven")
}

dependencies {
  compile(kotlinModule("stdlib", dependencyVersions["kotlin"]))
}
