rootProject.name = "yggdrasil"
include("yggdrasil-bootstrap", "yggdrasil-core", "yggdrasil-gradle-plugin", "yggdrasil-maven-plugin",
  "yggdrasil-gradle-plugin:yggdrasil-gradle-plugin-test", "yggdrasil-maven-plugin:yggdrasil-maven-plugin-test")
gradle.taskGraph.whenReady {
  gradle.taskGraph.allTasks.forEach {
    if (it.project.name in listOf("yggdrasil-gradle-plugin-test", "yggdrasil-maven-plugin-test")) {
      it.enabled = false
    }
  }
}
