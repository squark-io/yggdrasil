import org.gradle.script.lang.kotlin.archives
import org.gradle.script.lang.kotlin.base
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.gradleScriptKotlin
import org.gradle.script.lang.kotlin.repositories

allprojects {
  group = "io.squark.yggdrasil"
  version = "0.2.0-SNAPSHOT"

  repositories {
    gradleScriptKotlin()
    jcenter()
    mavenLocal()
    maven({
      setUrl("http://repository.jboss.org/nexus/content/groups/public")
    })
  }
}

plugins {
  base
}

dependencies {
  // Make the root project archives configuration depend on every subproject
  subprojects.forEach {
    archives(it)
  }
}
