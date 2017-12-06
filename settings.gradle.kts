rootProject.name = "yggdrasil"
include("yggdrasil-bootstrap", "yggdrasil-core", "yggdrasil-gradle-plugin", "yggdrasil-maven-plugin")
gradle.taskGraph.whenReady {
  gradle.taskGraph.allTasks.forEach {
    if (it.project.name == "yggdrasil-gradle-plugin:test") {
      it.onlyIf { false }
    }
  }
}
