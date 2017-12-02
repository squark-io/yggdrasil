package io.squark.yggdrasil.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.attributes.ImmutableAttributesFactory
import org.gradle.api.internal.java.JavaLibrary
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.invoke
import org.gradle.language.base.plugins.LifecycleBasePlugin
import javax.inject.Inject
import java.util.jar.Manifest as JdkManifest

/**
 * Yggdrasil Gradle Plugin
 *
 * Plugin for creating Yggdrasil applications conforming to the microprofile.io standard
 * @see <a href="http://microprofile.io">microprofile.io</a>
 *
 * Created by Erik Håkansson on 2017-03-25.
 * Copyright 2017
 *
 * @property objectFactory Gradle injected ObjectFactory
 * @property attributesFactory Gradle injected ImmutableAttributesFactory
 *
 */
class YggdrasilPlugin @Inject constructor(private val objectFactory: ObjectFactory,
                                          private val attributesFactory: ImmutableAttributesFactory) : Plugin<Project> {

  /**
   * Performs packaging of the Yggdrasil jar
   *
   * @param project The target object
   */
  override fun apply(project: Project) {
    project.plugins.apply(JavaPlugin::class.java)

    val yggdrasilVersion = javaClass.`package`.implementationVersion
    project.dependencies.add("compile", "io.squark.yggdrasil:yggdrasil-core:$yggdrasilVersion")
    project.configurations.create("yggdrasil-bootstrap")
    project.dependencies.add("yggdrasil-bootstrap", "io.squark.yggdrasil:yggdrasil-bootstrap:$yggdrasilVersion")

    project.run {
      tasks {
        YGGDRASIL_TASK(YggdrasilTask::class) {
          dependsOn("classes")
        }
      }
    }

    val yggdrasil = project.tasks.findByName(YGGDRASIL_TASK) as YggdrasilTask
    val jarArtifact = ArchivePublishArtifact(yggdrasil)
    project.extensions.getByType(DefaultArtifactPublicationSet::class.java).addCandidate(jarArtifact)
    project.components.add(JavaLibrary(objectFactory, project.configurations, attributesFactory, jarArtifact))

    project.tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(yggdrasil)
  }

  internal companion object {
    private const val YGGDRASIL_TASK = "yggdrasil"
  }
}
