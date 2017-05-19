import org.gradle.api.Project
import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.kotlinModule

/**
 * Configures the current project as a Kotlin project by applying the `kotlin`
 * plugin and adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {
  apply { it.plugin("kotlin") }
  dependencies {
    compile(kotlinModule("stdlib"))
  }
}